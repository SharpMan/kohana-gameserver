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
    public static void HandlePlayerStatusUpdateRequestMessage(WorldClient client, PlayerStatusUpdateRequestMessage Message) {
        if (PlayerStatusEnum.valueOf(Message.status.statusId) == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        client.getCharacter().setStatus(PlayerStatusEnum.valueOf(Message.status.statusId));
        if (client.getCharacter().getFight() != null) {
            client.getCharacter().getFight().sendToField(new PlayerStatusUpdateMessage(client.getAccount().id, client.getCharacter().getID(), Message.status));
        } else {
            client.getCharacter().getCurrentMap().sendToField(new PlayerStatusUpdateMessage(client.getAccount().id, client.getCharacter().getID(), Message.status));
        }
    }

}
