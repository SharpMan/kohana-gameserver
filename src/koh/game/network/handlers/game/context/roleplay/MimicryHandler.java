package koh.game.network.handlers.game.context.roleplay;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.FightState;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.inventory.items.*;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.types.game.data.items.ObjectItem;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;

/**
 * Created by Melancholia on 7/6/16.
 */
public class MimicryHandler {

    @HandlerAttribute(ID = MimicryObjectEraseRequestMessage.MESSAGE_ID)
    public static void handleMimicryObjectEraseRequestMessage(WorldClient client, MimicryObjectEraseRequestMessage message){
        if ((client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) || client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(message.hostUid);
        if (item == null || item.getPosition() != message.hostPos){
            client.send(new BasicNoOperationMessage());
            return;
        }
        if(item.getPosition() != 63){
            PlayerController.sendServerErrorMessage(client,"Desquipez vous cet item d'abord");
            return;
        }

        item.removeEffect(ACTION_ITEM_SKIN_ITEM);
        client.send(new ObjectModifiedMessage(item.getObjectItem()));
        client.send(new PopupWarningMessage((byte) 0," Symbiote","Deco/reco pour prendre effet"));
        PlayerController.sendServerMessage(client,"Deco/reco pour prendre effet");
    }




    private static final int ACTION_ITEM_SKIN_ITEM = 1151;

    //TODO close
    @HandlerAttribute(ID = MimicryObjectFeedAndAssociateRequestMessage.MESSAGE_ID)
    public static void handleMimicryObjectFeedAndAssociateRequestMessage(WorldClient client, MimicryObjectFeedAndAssociateRequestMessage message) {
        if ((client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) || client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(message.foodUID);
        final InventoryItem symbiote = client.getCharacter().getInventoryCache().find(message.symbioteUID), host = client.getCharacter().getInventoryCache().find(message.hostUID);

        if (item == null ||
                item.getPosition() != message.foodPos ||
                symbiote == null ||
                host == null ||
                host.getPosition() != message.hostPos ||
                symbiote.getTemplateId() != 14485 ||
                symbiote.getPosition() != message.symbiotePos) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if(host.getPosition() != 63 ||
                item.getPosition() != 63 ||
                item.getPosition() != 63 ||
                item.getTemplate().getTypeId() != host.getTemplate().getTypeId()){
            PlayerController.sendServerErrorMessage(client,"Desquipez vous ces items d'abord");
            return;
        }

        if(item.getTemplate().getLevel() > host.getTemplate().getLevel()){
            PlayerController.sendServerErrorMessage(client, "le niveau de la symbiote est plus grand que celui de la haute");
            return;
        }

        host.removeEffect(ACTION_ITEM_SKIN_ITEM);
        host.getEffects$Notify().add(new ObjectEffectInteger(ACTION_ITEM_SKIN_ITEM, item.getTemplateId()));
        //host.getEffects().add(new ObjectEffectInteger(1152,14553));

        final ObjectItem hostObjectItem = host.getObjectItem();



       /* if(message.preview)
            client.send(new MimicryObjectPreviewMessage(hostObjectItem));*/
        client.send(new MimicryObjectPreviewMessage(hostObjectItem));
        client.send(new ObjectModifiedMessage(hostObjectItem));
        client.getCharacter().getInventoryCache().safeDelete(item,1);
        client.getCharacter().getInventoryCache().safeDelete(symbiote,1);
                //client.send(new MimicryObjectAssociatedMessage(host.getID()));

    }

}
