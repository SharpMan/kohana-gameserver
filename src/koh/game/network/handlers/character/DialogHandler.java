package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.requests.ExchangeRequest;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class DialogHandler {

    @HandlerAttribute(ID = LeaveDialogRequestMessage.MESSAGE_ID)
    public static void HandleLeaveDialogRequestMessage(WorldClient Client, LeaveDialogRequestMessage Message) {
        try {
            if (Client.IsGameAction(GameActionTypeEnum.EXCHANGE)) {
                if (!Client.myExchange.CloseExchange()) {
                    Client.EndGameAction(GameActionTypeEnum.EXCHANGE);
                }
            } else if (Client.IsGameAction(GameActionTypeEnum.CREATE_GUILD)) {
                Client.EndGameAction(GameActionTypeEnum.CREATE_GUILD);
                Client.Send(new BasicNoOperationMessage());
            } else if (Client.IsGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                if (!(Client.GetBaseRequest() instanceof ExchangeRequest)) {
                    Client.Send(new BasicNoOperationMessage());
                }
                if (!Client.GetBaseRequest().Declin()) {
                    Client.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
                }
            } else if (Client.IsGameAction(GameActionTypeEnum.ZAAP)) {
                Client.EndGameAction(GameActionTypeEnum.ZAAP);
            } else if (Client.IsGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                Client.EndGameAction(GameActionTypeEnum.NPC_DAILOG);
            } else {
                Client.Send(new BasicNoOperationMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
