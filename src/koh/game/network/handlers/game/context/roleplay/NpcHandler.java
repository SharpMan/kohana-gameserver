package koh.game.network.handlers.game.context.roleplay;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import koh.game.actions.*;
import koh.game.controllers.PlayerController;
import koh.game.dao.NpcDAO;
import koh.game.entities.actors.Npc;
import koh.game.entities.actors.npc.NpcReply;
import koh.game.exchange.*;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.NpcActionTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcDialogReplyMessage;
import koh.protocol.messages.game.context.roleplay.npc.NpcGenericActionRequestMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkNpcShopMessage;

/**
 *
 * @author Neo-Craft
 */
public class NpcHandler {

    @HandlerAttribute(ID = NpcDialogReplyMessage.MESSAGE_ID)
    public static void HandleNpcDialogReplyMessage(WorldClient Client, NpcDialogReplyMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.NPC_DAILOG)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        ((NpcDialog)Client.GetGameAction(GameActionTypeEnum.NPC_DAILOG)).Reply(Message.replyId);

    }

    /*LeaveDialogRequestMessage*/
    @HandlerAttribute(ID = NpcGenericActionRequestMessage.MESSAGE_ID)
    public static void HandleNpcGenericActionRequestMessage(WorldClient Client, NpcGenericActionRequestMessage Message) {
        Npc PNJ = Client.Character.CurrentMap.GetNpc(Message.npcId);
        if (PNJ == null) {
            Client.Send(new BasicNoOperationMessage());
            throw new Error("Le pnj " + Message.npcId + " est absent");
        }
        // ExchangeStartedBidBuyerMessage
        //ExchangeTypesExchangerDescriptionForUserMessage
        if (Client.IsGameAction(GameActionTypeEnum.EXCHANGE)) {
            PlayerController.SendServerMessage(Client, "You're always in a exchange...");
            return;
        }
        final NpcActionTypeEnum Action = NpcActionTypeEnum.valueOf(Message.npcActionId);
        switch (Action) {
            case ACTION_BUY_SELL:
                if (Client.CanGameAction(GameActionTypeEnum.EXCHANGE)) {
                    Client.myExchange = new NpcExchange(Client, PNJ);
                    Client.AddGameAction(new GameExchange(Client.Character, Client.myExchange));
                    Client.Send(new ExchangeStartOkNpcShopMessage(PNJ.ID, PNJ.Template().CommonTokenId(), PNJ.Template().GetItems()));
                }
                break;
            case ACTION_TALK:
                if (Client.CanGameAction(GameActionTypeEnum.NPC_DAILOG)) {
                    Client.AddGameAction(new NpcDialog(PNJ, Client.Character));
                }
                break;
            default:
                PlayerController.SendServerMessage(Client, "Ce type de transaction n'est pas encore disponnible");
                return;
        }

    }

}
