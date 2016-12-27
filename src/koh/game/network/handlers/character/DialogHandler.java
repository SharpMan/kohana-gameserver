package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.requests.ExchangeRequest;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.roleplay.StopToListenRunningFightRequestMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.protocol.messages.game.dialog.LeaveDialogRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class DialogHandler {

    @HandlerAttribute(ID = StopToListenRunningFightRequestMessage.M_ID)
    public static void handleStopToListenRunningFightRequestMessage(WorldClient client,StopToListenRunningFightRequestMessage message){
//TODO:
    }

    @HandlerAttribute(ID = LeaveDialogRequestMessage.MESSAGE_ID)
    public static void handleLeaveDialogRequestMessage(WorldClient client, LeaveDialogRequestMessage message) {
        try {
            if (client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
                if (!client.getMyExchange().closeExchange()) {
                    client.endGameAction(GameActionTypeEnum.EXCHANGE);
                }
            }
            else if(client.isGameAction(GameActionTypeEnum.SPELL_UI)){
                client.endGameAction(GameActionTypeEnum.SPELL_UI);
            }
            else if (client.isGameAction(GameActionTypeEnum.CREATE_GUILD)) {
                client.endGameAction(GameActionTypeEnum.CREATE_GUILD);
                client.send(new BasicNoOperationMessage());
            } else if (client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
                if (!(client.getBaseRequest() instanceof ExchangeRequest)) {
                    client.send(new BasicNoOperationMessage());
                }
                if (!client.getBaseRequest().declin()) {
                    client.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
                }
            } else if (client.isGameAction(GameActionTypeEnum.ZAAP)) {
                client.endGameAction(GameActionTypeEnum.ZAAP);
            } else if (client.isGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                client.endGameAction(GameActionTypeEnum.NPC_DAILOG);
            }
            else if (client.isGameAction(GameActionTypeEnum.TAX_COLLECTOR_DIALOG)) {
                client.endGameAction(GameActionTypeEnum.TAX_COLLECTOR_DIALOG);
            }
            else {
                client.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_DIALOG));
                client.send(new BasicNoOperationMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
