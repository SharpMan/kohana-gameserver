package koh.game.network.handlers.game.context.roleplay;

import koh.game.dao.AreaDAO;
import koh.game.entities.environments.SubArea;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.messages.game.prism.PrismsListMessage;
import koh.protocol.messages.game.prism.PrismsListRegisterMessage;
import koh.protocol.types.game.prism.PrismSubareaEmptyInfo;

/**
 *
 * @author Neo-Craft
 */
public class PrismHandler {

    @HandlerAttribute(ID = PrismsListRegisterMessage.MESSAGE_ID)
    public static void HandlePrismsListRegisterMessage(WorldClient Client, Message message) {
        Client.Send(SubArea.PrismMessage());

    }

}
