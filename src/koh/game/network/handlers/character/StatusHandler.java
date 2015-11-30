package koh.game.network.handlers.character;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.PlayerStatusEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.character.status.PlayerStatusUpdateMessage;
import koh.protocol.messages.game.character.status.PlayerStatusUpdateRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class StatusHandler {

    @HandlerAttribute(ID = PlayerStatusUpdateRequestMessage.MESSAGE_ID)
    public static void HandlePlayerStatusUpdateRequestMessage(WorldClient Client, PlayerStatusUpdateRequestMessage Message) {
        if (PlayerStatusEnum.valueOf(Message.status.statusId) == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.character.status = PlayerStatusEnum.valueOf(Message.status.statusId);
        if (Client.character.getFight() != null) {
            Client.character.getFight().sendToField(new PlayerStatusUpdateMessage(Client.getAccount().id, Client.character.ID, Message.status));
        } else {
            Client.character.currentMap.sendToField(new PlayerStatusUpdateMessage(Client.getAccount().id, Client.character.ID, Message.status));
        }
    }

}
