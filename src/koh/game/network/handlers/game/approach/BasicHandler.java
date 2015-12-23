package koh.game.network.handlers.game.approach;

import koh.game.dao.DAO;
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
    public static void HandleBasicWhoIsRequestMessage(WorldClient Client, BasicWhoIsRequestMessage message) {
        //TODO: Maybe anti flood
        Player victim = DAO.getPlayers().getCharacter(message.search.toLowerCase());
        if (victim == null) {
            Client.send(new BasicWhoIsNoMatchMessage(message.search));
            return;
        }
        /*AlianceInfo GuildInfo */
        // TODO AreaID;

        Client.send(new BasicWhoIsMessage(message.search.equals(victim.getNickName()), victim.getAccount().right, victim.getAccount().nickName, victim.getAccount().id, victim.getNickName(), victim.getID(), (short) 0, new AbstractSocialGroupInfos[0], message.verbose, victim.getStatus().value()));
    }

    @HandlerAttribute(ID = BasicLatencyStatsMessage.MESSAGE_ID)
    public static void HandleBasicLatencyStatsMessage(WorldClient Client, BasicLatencyStatsMessage Message) {
        Client.setLatency(Message);
        if (Client.getCallBacks() != null) {
            Client.getCallBacks().first.sendToField(Client.getCallBacks().second);
            Client.getCallBacks().Clear();
            Client.setCallBacks(null);
        }
        Client.sequenceMessage();
    }

    @HandlerAttribute(ID = BasicStatMessage.MESSAGE_ID)
    public static void HandleBasicStatMessage(WorldClient Client, Message Message) {
        //Todo Switch StatisticTypeEnum
        Client.sequenceMessage(new BasicNoOperationMessage());
    }

}
