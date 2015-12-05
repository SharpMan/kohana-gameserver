package koh.game.network.handlers.game.approach;

import koh.game.dao.DAO;
import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.*;
import koh.protocol.types.game.social.AbstractSocialGroupInfos;

/**
 *
 * @author Neo-Craft
 */
public class BasicHandler {

    @HandlerAttribute(ID = BasicWhoIsRequestMessage.MESSAGE_ID)
    public static void HandleBasicWhoIsRequestMessage(WorldClient Client, BasicWhoIsRequestMessage Message) {
        //TODO: Maybe anti flood
        Player Shearch = DAO.getPlayers().getCharacter(Message.search.toLowerCase());
        if (Shearch == null) {
            Client.send(new BasicWhoIsNoMatchMessage(Message.search));
            return;
        }
        /*AlianceInfo GuildInfo */
        // TODO AreaID;

        Client.send(new BasicWhoIsMessage(Message.search.equals(Shearch.nickName), Shearch.account.right, Shearch.account.nickName, Shearch.account.id, Shearch.nickName, Shearch.ID, (short) 0, new AbstractSocialGroupInfos[0], Message.verbose, Shearch.status.value()));
    }

    @HandlerAttribute(ID = BasicLatencyStatsMessage.MESSAGE_ID)
    public static void HandleBasicLatencyStatsMessage(WorldClient Client, BasicLatencyStatsMessage Message) {
        Client.latency = Message;
        if (Client.callBacks != null) {
            Client.callBacks.first.sendToField(Client.callBacks.second);
            Client.callBacks.Clear();
            Client.callBacks = null;
        }
        Client.sequenceMessage();
    }

    @HandlerAttribute(ID = BasicStatMessage.MESSAGE_ID)
    public static void HandleBasicStatMessage(WorldClient Client, Message Message) {
        //Todo Switch StatisticTypeEnum
        Client.sequenceMessage(new BasicNoOperationMessage());
    }

}
