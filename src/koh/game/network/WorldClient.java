package koh.game.network;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.d2o.Couple;
import koh.game.Main;
import koh.game.actions.GameAction;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.GameBaseRequest;
import koh.game.actions.requests.PartyRequest;
import koh.game.dao.mysql.AccountDataDAOImpl;
import koh.game.dao.mysql.AccountOccupedException;
import koh.game.dao.mysql.PlayerDAO;
import koh.game.entities.Account;
import koh.game.entities.AccountTicket;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.entities.environments.DofusTrigger;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.exchange.Exchange;
import koh.game.executors.Waiter;
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
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Neo-Craft
 */
public class WorldClient {

    private final IoSession session;
    public AccountTicket tempTicket;
    public boolean showQueue;
    public Player Character;
    public String ClientKey;
    private final Map<GameActionTypeEnum, GameAction> myActions = Collections.synchronizedMap(new HashMap<GameActionTypeEnum, GameAction>());
    public int lastPacketId, Seq = 0;
    public boolean firstCheck = true;
    public BasicLatencyStatsMessage Latency;
    public volatile Couple<IWorldEventObserver, Message> CallBacks;
    public volatile DofusTrigger onMouvementConfirm = null;
    public Exchange myExchange = null;
    private GameBaseRequest myBaseRequest = null;
    private CopyOnWriteArrayList<PartyRequest> PartyRequests;

    public Map<Byte, Long> LastChannelMessage = new HashMap<Byte, Long>() {
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

    public void AbortGameActions() {
        synchronized (myActions) {
            myActions.values().stream().forEach((action) -> {
                action.Abort(new Object[0]);
            });
        }
        this.myActions.clear();
    }

    public boolean CanGameAction(GameActionTypeEnum ActionType) {
        synchronized (this.myActions) {
            this.myActions.values().stream().forEach(x -> System.out.println(x.ActionType));
            return this.myActions.values().stream().allMatch(x -> x.CanSubAction(ActionType));
        }
    }

    public void addPartyRequest(PartyRequest req) {
        if (this.PartyRequests == null) {
            this.PartyRequests = new CopyOnWriteArrayList<>();
        }
        this.PartyRequests.add(req);
    }

    public void removePartyRequest(PartyRequest req) {
        this.PartyRequests.remove(req);
    }

    public PartyRequest getPartyRequest(int id, int guestId) {
        try {
            return this.PartyRequests.stream().filter(x -> x.Requester.GetParty() != null && x.Requester.GetParty().ID == id && x.Requested.Character.ID == guestId).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public PartyRequest getPartyRequest(int id) {
        try {
            return this.PartyRequests.stream().filter(x -> x.Requester.GetParty() != null && x.Requester.GetParty().ID == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public void AddGameAction(GameAction Action) {
        try {
            synchronized (this.myActions) {
                this.myActions.put(Action.ActionType, Action);
            }
            Action.RegisterEnd(WorldClient.class.getMethod("DelGameAction", GameAction.class));
            Action.Execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getGameActionCount() {
        return this.myActions.size();
    }

    public void EndGameAction(GameActionTypeEnum Action) {
        try {
            synchronized (this.myActions) {
                if (this.myActions.containsKey(Action)) {
                    this.myActions.get(Action).EndExecute();
                    this.myActions.remove(Action);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GameAction GetGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            if (this.myActions.containsKey(Action)) {
                return this.myActions.get(Action);
            }
            return null;
        }
    }

    public Party GetParty() {
        try {
            return ((GameParty) GetGameAction(GameActionTypeEnum.GROUP)).Party;
        } catch (Exception e) {
            return null;
        }
    }

    public void AbortGameAction(GameActionTypeEnum Action, Object[] Args) {
        synchronized (this.myActions) {
            if (this.myActions.containsKey(Action)) {
                this.myActions.get(Action).Abort(Args);
            }
        }
    }

    public void DelGameAction(GameAction GameAction) {
        synchronized (this.myActions) {
            this.myActions.remove(GameAction.ActionType);
        }
    }

    public void DelGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            this.myActions.remove(Action);
        }
    }

    

    public boolean IsGameAction(GameActionTypeEnum Action) {
        synchronized (this.myActions) {
            return this.myActions.containsKey(Action);
        }
    }

    public String getIP() {
        return ((InetSocketAddress) this.session.getRemoteAddress()).getAddress().toString();
    }

    public Account getAccount() {
        return tempTicket.valid();
    }

    public void parsePacket(Message message) { //Synchronized ?
        try {
            if (message == null) {
                return;
            }
            if (message instanceof BasicPingMessage) {
                this.Send(new BasicPongMessage(((BasicPingMessage) message).quiet));
                return;
            }

            this.lastPacketId = message.getMessageId();
            this.Seq++;
            Method MessageIdentifier = Handler.getMethodByMessage(message.getMessageId());
            if (MessageIdentifier != null) {
                MessageIdentifier.invoke(null, this, message);
            } else {
                Main.Logs().writeInfo("Packet not handled " + message.getMessageId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param packet
     */
    public void Send(Message packet) {
        if (packet == null) {
            return;
        }

        session.write(packet);
    }

    public void SequenceMessage() {
        this.Send(new BasicAckMessage(Seq, lastPacketId, null));
    }

    public void SequenceMessage(Message Dofus) {
        this.Send(new BasicAckMessage(Seq, lastPacketId, Dofus));
    }

    public void timeOut() {
        close();
    }

    public void setTimeout(int ms) {
        //TODO : Timer 
    }

    public void threatWaiting() {
        try {
            PlayerDAO.FindAll(this.getAccount());
            if (this.getAccount().Data == null) {
                this.getAccount().Data = AccountDataDAOImpl.Find(this.getAccount().ID);
            }
            if (showQueue) {
                this.Send(new LoginQueueStatusMessage((short) 0, (short) 0));
            }
            this.Send(new AuthenticationTicketAcceptedMessage());
            this.Send(new BasicTimeMessage((double) (new Date().getTime()), 0));
            this.Send(new ServerOptionalFeaturesMessage(new byte[]{1, 2}));
            this.Send(new AccountCapabilitiesMessage(getAccount().ID, false, (short) Integer.MAX_VALUE, (short) Integer.MAX_VALUE, this.getAccount().Right));
            this.Send(new TrustStatusMessage(true));
            if (this.getAccount().Right > 0) {
                this.Send(new ConsoleCommandsListMessage(new String[]{"infos"}, new String[]{"infos"}, new String[]{"infos"}));
            }
        } catch (AccountOccupedException ex) {
            new Waiter(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void close() {
        try {
            this.AbortGameActions();
        } catch (Exception ex) {
        }
        if (this.PartyRequests != null) {
            for (PartyRequest req : this.PartyRequests) {
                if (req.Requester == this) {
                    req.Abort();
                } else {
                    req.Declin();
                }
            }

            this.PartyRequests.clear();
            this.PartyRequests = null;
        }
        if (session != null && !session.isClosing()) {
            session.close(true);
        }

        this.showQueue = false;
        if (this.Character != null) {
            Main.Logs().writeDebug("Player " + this.Character.NickName + " disconnected");
            ChatChannel.UnRegister(this);
            this.Character.OnDisconnect();
            this.Character = null;
        }
        if (tempTicket != null) {
            WorldServer.Loader.onClientDisconnect(this);
            tempTicket.clear();
            tempTicket = null;
        }

    }

    public void SetBaseRequest(GameBaseRequest Request) {
        this.myBaseRequest = Request;
    }

    public GameBaseRequest GetBaseRequest() {
        return this.myBaseRequest;
    }

}
