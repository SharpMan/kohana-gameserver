package koh.game.network.handlers.game.context.roleplay;

import koh.game.actions.*;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Npc;
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
    public static void HandleNpcDialogReplyMessage(WorldClient Client, NpcDialogReplyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.NPC_DAILOG)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        ((NpcDialog) Client.getGameAction(GameActionTypeEnum.NPC_DAILOG)).reply(Message.replyId);

    }

    /*LeaveDialogRequestMessage*/
    @HandlerAttribute(ID = NpcGenericActionRequestMessage.MESSAGE_ID)
    public static void HandleNpcGenericActionRequestMessage(WorldClient Client, NpcGenericActionRequestMessage Message) {
        Npc PNJ = Client.character.getCurrentMap().getNpc(Message.npcId);
        if (PNJ == null) {
            Client.send(new BasicNoOperationMessage());
            throw new Error("Le pnj " + Message.npcId + " est absent");
        }
        // ExchangeStartedBidBuyerMessage
        //ExchangeTypesExchangerDescriptionForUserMessage
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            PlayerController.sendServerMessage(Client, "You're always in a exchange...");
            return;
        }
        final NpcActionTypeEnum Action = NpcActionTypeEnum.valueOf(Message.npcActionId);
        if (Action == null) {
            logger.error("Unknow action {} by character {}", Message.npcActionId, Client.getCharacter().getNickName());
            return;
        } else if (!ArrayUtils.contains(PNJ.getTemplate().getActions(), Message.npcActionId)) {
            PlayerController.sendServerMessage(Client, "Ce type de transaction n'est pas encore disponnible");
            return;
        }
        switch (Action) {
            case ACTION_BUY_SELL:
                if (Client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
                    Client.myExchange = new NpcExchange(Client, PNJ);
                    Client.addGameAction(new GameExchange(Client.character, Client.myExchange));
                    Client.send(new ExchangeStartOkNpcShopMessage(PNJ.getID(), PNJ.getTemplate().getCommonTokenId(), PNJ.getTemplate().getItems$Array()));
                }
                break;
            case ACTION_TALK:
                if (Client.canGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                    Client.addGameAction(new NpcDialog(PNJ, Client.character));
                }
                break;
            default:
                PlayerController.sendServerMessage(Client, "Ce type de transaction n'est pas encore disponnible");
                return;
        }

    }

}
