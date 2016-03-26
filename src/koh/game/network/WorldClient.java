package koh.game.network;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private GameBaseRequest myBaseRequest;
    private CopyOnWriteArrayList<PartyRequest> partyRequests;
    private static final Logger logger = LogManager.getLogger(WorldClient.class);

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
        return this.myActions.size();
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
        return this.getRemoteAddress().getAddress().getHostAddress();
    }

    public Account getAccount() {
        return tempTicket.valid();
    }

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
                messageIdentifier.invoke(null, this, message);
            } else {
                logger.error("Packet not handled {}" , message.getMessageId());
            }
        } catch (Exception e) {
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

        session.write(packet);
    }

    public void sequenceMessage() {
        //TODO : Right seq and lastPacketId value
        this.send(new BasicAckMessage(sequenceMessage, lastPacketId));
    }

    public void sequenceMessage(Message Dofus) {
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
            this.send(new AccountCapabilitiesMessage(getAccount().id, false, (short) Integer.MAX_VALUE, (short) Integer.MAX_VALUE, this.getAccount().right));
            this.send(new TrustStatusMessage(true));
            if (this.getAccount().right > 0) {
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

        this.showQueue = false;
        if (this.character != null) {
            logger.debug("Player {} disconnected",this.character.getNickName());
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

    public void setBaseRequest(GameBaseRequest Request) {
        this.myBaseRequest = Request;
    }

    public GameBaseRequest getBaseRequest() {
        return this.myBaseRequest;
    }

}
