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
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        Client.Character.Status = PlayerStatusEnum.valueOf(Message.status.statusId);
        if (Client.Character.GetFight() != null) {
            Client.Character.GetFight().sendToField(new PlayerStatusUpdateMessage(Client.getAccount().ID, Client.Character.ID, Message.status));
        } else {
            Client.Character.CurrentMap.sendToField(new PlayerStatusUpdateMessage(Client.getAccount().ID, Client.Character.ID, Message.status));
        }
    }

}
