package koh.game.network;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import koh.d2o.Couple;
import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.GameWaiting;
import koh.game.actions.requests.GameBaseRequest;
import koh.game.actions.requests.PartyRequest;
import koh.game.dao.DAO;
import koh.game.dao.mysql.AccountOccupedException;
import koh.game.entities.Account;
import koh.game.entities.AccountTicket;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.entities.environments.DofusTrigger;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.exchange.Exchange;
import koh.game.fights.FightTypeEnum;
import koh.game.network.handlers.Handler;
import koh.game.network.handlers.character.AuthorizedHandler;
import koh.game.network.handlers.game.approach.ApproachHandler;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.protocol.client.Message;
import static koh.protocol.client.enums.ChatActivableChannelsEnum.*;
import koh.protocol.messages.authorized.ConsoleCommandsListMessage;
import koh.protocol.messages.common.basic.BasicPingMessage;
import koh.protocol.messages.common.basic.BasicPongMessage;
import koh.protocol.messages.connection.LoginQueueStatusMessage;
import koh.protocol.messages.game.approach.AccountCapabilitiesMessage;
import koh.protocol.messages.game.approach.AuthenticationTicketAcceptedMessage;
import koh.protocol.messages.game.approach.ServerOptionalFeaturesMessage;
import koh.protocol.messages.game.basic.BasicAckMessage;
import koh.protocol.messages.game.basic.BasicLatencyStatsMessage;
import koh.protocol.messages.game.basic.BasicTimeMessage;
import koh.protocol.messages.secure.TrustStatusMessage;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Neo-Craft
 */
public class WorldClient {

    private final IoSession session;
    @Getter @Setter
    private AccountTicket tempTicket;
    @Getter @Setter
    private boolean showQueue;
    @Getter @Setter
    private volatile boolean hasSentTicket = false;
    @Getter @Setter
    private Player character;
    @Getter
    private Object $mutex = new Object();
    @Setter
    private String clientKey;
    private final Map<GameActionTypeEnum, GameAction> myActions = new ConcurrentHashMap<>();
    private int lastPacketId, sequenceMessage;
    @Setter
    private BasicLatencyStatsMessage latency;
    @Getter @Setter
    private volatile Couple<IWorldEventObserver, Message> callBacks;
    @Getter @Setter
    private volatile DofusTrigger onMouvementConfirm;
    @Getter @Setter
    public Exchange myExchange;
    @Getter @Setter
    private long lastCommand;
    private GameBaseRequest myBaseRequest;
    @Getter
    private CopyOnWriteArrayList<PartyRequest> partyRequests;
    private static final Logger logger = LogManager.getLogger(WorldClient.class);
    private Map<String, Long> packetClock = new ConcurrentHashMap<>();
    @Setter
    private String address;


    @Getter
    public Map<Byte, Long> lastChannelMessage = new HashMap<Byte, Long>(7) {
        {
            put(CHANNEL_SALES, 0L);
            put(CHANNEL_SEEK, 0L);
            put(CHANNEL_NOOB, 0L);
            put(CHANNEL_ADS, 0L);
            put(SMILEY_CHANNEL, 0L);
            put(GM_CHANNEL, 0L);
        }
    };

    WorldClient(IoSession session) {
        this.session = session;
    }

    public void abortGameActions() {
        synchronized (myActions) {
            myActions.values().stream().forEach((action) -> {
                action.abort(new Object[0]);
            });
        }
        this.myActions.clear();
    }

    public void abortGameActionsExceptGroup() {
        synchronized (myActions) {
            myActions.entrySet().stream().forEach((action) -> {
                if(!(action.getValue() instanceof GameParty)){
                    action.getValue().abort(new Object[0]);
                    myActions.remove(action.getKey());
                }
            });
        }
    }


    public boolean canGameAction(GameActionTypeEnum ActionType) {
        synchronized (this.myActions) {
            //this.myActions.values().stream().forEach(x -> System.out.println(x.actionType));
            return this.myActions.values().stream().allMatch(x -> x.canSubAction(ActionType));
        }
    }

    public void addPartyRequest(PartyRequest req) {
        if (this.partyRequests == null) {
            this.partyRequests = new CopyOnWriteArrayList<>();
        }
        this.partyRequests.add(req);
    }

    public void removePartyRequest(PartyRequest req) {
        this.partyRequests.remove(req);
    }

    public PartyRequest getPartyRequest(int id, int guestId) {
        if(this.partyRequests == null)
            return null;
       return this.partyRequests.stream().filter(x -> x.requester.getParty() != null && x.requester.getParty().id == id && x.requested.character.getID() == guestId).findFirst().orElse(null);

    }

    public PartyRequest getPartyRequest(int id) {
        try {
            return this.partyRequests.stream().filter(x -> x.requester.getParty() != null && x.requester.getParty().id == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public void addGameAction(GameAction Action) {
        try {
            synchronized (this.myActions) {
                this.myActions.put(Action.actionType, Action);
            }
            Action.registerEnd(WorldClient.class.getMethod("delGameAction", GameAction.class));
            Action.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getGameActionCount() {
        this.myActions.values().forEach(System.out::println);
        return this.myActions.size();
    }

    public synchronized boolean canParsePacket(String packet, final long delay){
        final Long lastReq = this.packetClock.get(packet);
        if(lastReq == null){
            this.packetClock.put(packet, System.currentTimeMillis());
        }else{
            if(System.currentTimeMillis() - lastReq < delay){
                return false;
            }
            this.packetClock.replace(packet, System.currentTimeMillis());
        }
        return true;
    }


    public void endGameAction(GameActionTypeEnum action) {
        try {
            synchronized (this.myActions) {
                if (this.myActions.containsKey(action)) {
                    this.myActions.get(action).endExecute();
                    this.myActions.remove(action);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameAction getGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            if (this.myActions.containsKey(Action)) {
                return this.myActions.get(Action);
            }
            return null;
        }
    }

    public Party getParty() {
        try {
            return ((GameParty) getGameAction(GameActionTypeEnum.GROUP)).party;
        } catch (Exception e) {
            return null;
        }
    }



    public void abortGameAction(GameActionTypeEnum Action, Object[] Args) {
        synchronized (this.myActions) {
            if (this.myActions.containsKey(Action)) {
                this.myActions.get(Action).abort(Args);
            }
        }
    }

    public void delGameAction(GameAction GameAction) {
        synchronized (this.myActions) {
            this.myActions.remove(GameAction.actionType);
        }
    }

    public void delGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            this.myActions.remove(Action);
        }
    }

    

    public boolean isGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            return this.myActions.containsKey(Action);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) session.getRemoteAddress();
    }

    public String getIP() {
        if(address != null)
            return address;
        return this.getRemoteAddress().getAddress().getHostAddress();
    }

    public Account getAccount() {
        return tempTicket.valid();
    }

    private int nb = 0;

    public void parsePacket(Message message) {
        try {
            if (message == null) {
                return;
            }
            if (message instanceof BasicPingMessage) {
                this.send(new BasicPongMessage(((BasicPingMessage) message).quiet));
                return;
            }

            this.lastPacketId = message.getMessageId();
            this.sequenceMessage++;
            final Method messageIdentifier = Handler.getMethodByMessage(message.getMessageId());
            if (messageIdentifier != null) {
                if(character == null){
                    if(!(messageIdentifier.getDeclaringClass().isAssignableFrom(ApproachHandler.class) || messageIdentifier.getDeclaringClass().isAssignableFrom(CharacterHandler.class) || messageIdentifier.getDeclaringClass().isAssignableFrom(AuthorizedHandler.class))){
                        return;
                    }
                }
                messageIdentifier.invoke(null, this, message);
            }
            else {
                logger.info("Packet not handled {}" , message.getMessageId());
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
            nb++;
            if(nb == 3){
                this.forceClose();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param packet
     */
    public void send(Message packet) {
        if (packet == null) {
            return;
        }
        //this.log((logger) -> logger.info();

        session.write(packet);
    }

    public void sequenceMessage() {
        //TODO : Right seq and lastPacketId value
        this.send(new BasicAckMessage(sequenceMessage, lastPacketId));
    }

    public void sequenceMessage(Message msg) {
        this.send(msg);
        //TODO: SequenceMessage BASIC + Dofus blinded
        //this.send(new BasicAckMessage(sequenceMessage, lastPacketId, Dofus));
    }

    public void timeOut() {
        close();
    }

    private final ConsoleCommandsListMessage consolePregenMessage = new ConsoleCommandsListMessage(new String[0], new String[0], new String[0]);


    public void threatWaiting() {
        try {
            if(DAO.getPlayers().isLocked()){
                if (DAO.getPlayers().isCurrentlyOnProcess(this.getAccount().id)
                        || this.getAccount().characters.parallelStream().anyMatch(Player -> Player.getFighter() != null && Player.getFight() != null && Player.getFight().getFightType() == FightTypeEnum.FIGHT_TYPE_CHALLENGE)) {
                    throw new AccountOccupedException("");
                }
            }

            DAO.getPlayers().getByAccount(this.getAccount());
            if (this.getAccount().accountData == null) {
                this.getAccount().accountData = DAO.getAccountDatas().get(this.getAccount().id);
            }
            if (showQueue) {
                this.send(new LoginQueueStatusMessage((short) 0, (short) 0));
            }
            this.send(new AuthenticationTicketAcceptedMessage());
            this.send(new BasicTimeMessage((double) (new Date().getTime()), 0));
            this.send(new ServerOptionalFeaturesMessage(new byte[]{1, 2}));
            this.send(new AccountCapabilitiesMessage(getAccount().id, false, (short) Integer.MAX_VALUE, (short) Integer.MAX_VALUE, this.getAccount().accountData.right));
            this.send(new TrustStatusMessage(true));
            if (this.getAccount().accountData.right > 0) {
                this.send(consolePregenMessage);
            }
        } catch (AccountOccupedException ex) {
           this.addGameAction(new GameWaiting(this));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void close() {
        try {
            this.abortGameActions();
        } catch (Exception ex) {
        }
        if (this.partyRequests != null) {
            for (PartyRequest req : this.partyRequests) {
                if (req.requester == this) {
                    req.abort();
                } else {
                    req.declin();
                }
            }

            this.partyRequests.clear();
            this.partyRequests = null;
        }
        if (session != null && !session.isClosing()) {
            session.close(true);
        }
        if(packetClock != null){
            packetClock.clear();
            packetClock = null;
        }

        this.showQueue = false;
        if (this.character != null) {
            logger.debug("Player disconnected",this.character.getNickName());
            ChatChannel.unRegister(this);
            this.character.onDisconnect();
            this.character = null;
        }
        if (tempTicket != null) {
            WorldServer.gameLoader.onClientDisconnect(this);
            tempTicket.clear();
            tempTicket = null;
        }

    }

    /*public void log(Consumer<Logger> writer) {
        if(session.isClosing() || !session.isConnected())
            return;
        try {
            ThreadContext.put("clientAddress", this.getRemoteAddress().getAddress().getHostAddress());
            if (character != null && character.getNickName() != null) {
                ThreadContext.put("plName", character.getNickName());
                ThreadContext.put("plId", Integer.toString(character.getID()));
            }
            try {
                writer.accept(logger);
            } finally {
                ThreadContext.remove("clientAddress");
                if (character != null && character.getNickName() != null) {
                    ThreadContext.remove("plName");
                    ThreadContext.remove("plId");
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }*/

    public void forceClose(){
        this.session.close(true);
    }

    public void setBaseRequest(GameBaseRequest Request) {
        this.myBaseRequest = Request;
    }

    public GameBaseRequest getBaseRequest() {
        return this.myBaseRequest;
    }

}
