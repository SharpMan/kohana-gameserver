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
            if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
                if (!Client.getMyExchange().closeExchange()) {
                    Client.endGameAction(GameActionTypeEnum.EXCHANGE);
                }
            } else if (Client.isGameAction(GameActionTypeEnum.CREATE_GUILD)) {
                Client.endGameAction(GameActionTypeEnum.CREATE_GUILD);
                Client.send(new BasicNoOperationMessage());
            } else if (Client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                if (!(Client.getBaseRequest() instanceof ExchangeRequest)) {
                    Client.send(new BasicNoOperationMessage());
                }
                if (!Client.getBaseRequest().declin()) {
                    Client.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
                }
            } else if (Client.isGameAction(GameActionTypeEnum.ZAAP)) {
                Client.endGameAction(GameActionTypeEnum.ZAAP);
            } else if (Client.isGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                Client.endGameAction(GameActionTypeEnum.NPC_DAILOG);
            } else {
                Client.send(new BasicNoOperationMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
