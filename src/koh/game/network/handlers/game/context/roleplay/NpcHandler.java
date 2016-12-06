package koh.game.network.handlers.game.context.roleplay;

import koh.game.actions.*;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.TaxCollector;
import koh.game.exchange.*;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.NpcActionTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogReplyMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcGenericActionRequestMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkNpcShopMessage;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class NpcHandler {

    private static final Logger logger = LogManager.getLogger(NpcHandler.class);

    @HandlerAttribute(ID = NpcDialogReplyMessage.MESSAGE_ID)
    public static void HandleNpcDialogReplyMessage(WorldClient client, NpcDialogReplyMessage Message) {
        if (!client.isGameAction(GameActionTypeEnum.NPC_DAILOG)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        ((NpcDialog) client.getGameAction(GameActionTypeEnum.NPC_DAILOG)).reply(Message.replyId);

    }

    /*LeaveDialogRequestMessage*/
    @HandlerAttribute(ID = NpcGenericActionRequestMessage.MESSAGE_ID)
    public static void handleNpcGenericActionRequestMessage(WorldClient client, NpcGenericActionRequestMessage message) {
        if(client.getCharacter() == null || client.getCharacter().getCurrentMap() == null){
            return;
        }
        final IGameActor actor = client.getCharacter().getCurrentMap().getActor(message.npcId);
        if(actor == null){
            if(client.getCharacter().isOnTutorial())
                return;
            client.send(new BasicNoOperationMessage());
            throw new Error("Le pnj " + message.npcId + " est absent");
        }
        else if (actor instanceof TaxCollector){
            final NpcActionTypeEnum action = NpcActionTypeEnum.valueOf(message.npcActionId);
            if (action == null) {
                logger.error("Unknow action {} by character {}", message.npcActionId, client.getCharacter().getNickName());
                return;
            }
            if(action == NpcActionTypeEnum.ACTION_TALK){
                if (client.canGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                    client.addGameAction(new TaxCollectorDialog((TaxCollector) actor, client.getCharacter()));
                }
            }
            System.out.println(action);
            return;

        }


        final Npc PNJ = client.getCharacter().getCurrentMap().getNpc(message.npcId);
        // ExchangeStartedBidBuyerMessage
        //ExchangeTypesExchangerDescriptionForUserMessage
        if (client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            PlayerController.sendServerMessage(client, "You're always in a exchange...");
            return;
        }
        final NpcActionTypeEnum action = NpcActionTypeEnum.valueOf(message.npcActionId);
        if (action == null) {
            logger.error("Unknow action {} by character {}", message.npcActionId, client.getCharacter().getNickName());
            return;
        } else if (!ArrayUtils.contains(PNJ.getTemplate().getActions(), message.npcActionId)) {
            PlayerController.sendServerMessage(client, "Ce type de transaction n'est pas encore disponnible");
            return;
        }
        switch (action) {
            case ACTION_BUY2:
            //case ACTION_BUY:
            case ACTION_BUY_SELL:
                if (client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
                    client.setMyExchange(new NpcExchange(client, PNJ));
                    client.addGameAction(new GameExchange(client.getCharacter(), client.getMyExchange()));
                    client.send(new ExchangeStartOkNpcShopMessage(PNJ.getID(), PNJ.getTemplate().getCommonTokenId(), PNJ.getTemplate().getItems$Array()));
                }
                break;
            case ACTION_TALK:
                if (client.canGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                    client.addGameAction(new NpcDialog(PNJ, client.getCharacter()));
                }
                break;
            default:
                PlayerController.sendServerMessage(client, "Ce type de transaction n'est pas encore disponnible");
                return;
        }

    }

}
