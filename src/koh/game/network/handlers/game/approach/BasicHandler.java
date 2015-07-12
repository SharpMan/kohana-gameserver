package koh.game.network.handlers.game.approach;

import koh.game.dao.PlayerDAO;
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
        Player Shearch = PlayerDAO.GetCharacter(Message.search.toLowerCase());
        if (Shearch == null) {
            Client.Send(new BasicWhoIsNoMatchMessage(Message.search));
            return;
        }
        /*AlianceInfo GuildInfo */
        // TODO AreaID;

        Client.Send(new BasicWhoIsMessage(Message.search.equals(Shearch.NickName), Shearch.Account.Right, Shearch.Account.NickName, Shearch.Account.ID, Shearch.NickName, Shearch.ID, (short) 0, new AbstractSocialGroupInfos[0], Message.verbose, Shearch.Status.value()));
    }

    @HandlerAttribute(ID = BasicLatencyStatsMessage.MESSAGE_ID)
    public static void HandleBasicLatencyStatsMessage(WorldClient Client, BasicLatencyStatsMessage Message) {
        Client.Latency = Message;
        if (Client.CallBacks != null) {
            Client.CallBacks.first.sendToField(Client.CallBacks.second);
            Client.CallBacks.Clear();
            Client.CallBacks = null;
        }
        Client.SequenceMessage();
    }

    @HandlerAttribute(ID = BasicStatMessage.MESSAGE_ID)
    public static void HandleBasicStatMessage(WorldClient Client, Message Message) {
        //Todo Switch StatisticTypeEnum
        Client.SequenceMessage(new BasicNoOperationMessage());
    }

}
