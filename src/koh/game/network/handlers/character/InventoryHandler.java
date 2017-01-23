package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemLivingObject;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.game.fights.FightState;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.inventory.InventoryWeightMessage;
import koh.protocol.messages.game.inventory.items.*;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.ObjectEffectDate;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Neo-Craft
 */
@Log4j2
public class InventoryHandler {




    @HandlerAttribute(ID = ObjectUseMultipleMessage.MESSAGE_ID)
    public static void handleObjectUseMultipleMessage(WorldClient client, ObjectUseMultipleMessage message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        InventoryItem item = client.getCharacter().getInventoryCache().find(message.objectUID);
        if (item.getQuantity() < message.quantity || item == null || !item.areConditionFilled(client.getCharacter())) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DESTROY));
            return;
        }
        //int i = 0;
        for (int i = 0; message.quantity > i; i++) {
            if (!item.getTemplate().use(client.getCharacter(), client.getCharacter(), client.getCharacter().getCell().getId())) {
                client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DESTROY));
                break;
            }
        }
        //client.getCharacter().getInventoryCache().safeDelete(item, i);
    }

    @HandlerAttribute(ID = ObjectUseOnCellMessage.MESSAGE_ID)
    public static void handleObjectUseOnCellMessage(WorldClient client, ObjectUseOnCellMessage message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(message.objectUID);
        if (item == null || !item.areConditionFilled(client.getCharacter()) || !item.getTemplate().use(client.getCharacter(), client.getCharacter(), message.cell)) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DESTROY));
            return;
        }
    }

    @HandlerAttribute(ID = ObjectUseOnCharacterMessage.MESSAGE_ID)
    public static void handleObjectUseOnCharacterMessage(WorldClient client, ObjectUseOnCharacterMessage message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final Player target = client.getCharacter().getCurrentMap().getPlayer(message.characterId);
        final InventoryItem item = client.getCharacter().getInventoryCache().find(message.objectUID);

        if (target == null || item == null || !item.areConditionFilled(target) || item.getTemplate().use(client.getCharacter(), target, target.getCell().getId())) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DESTROY));
            return;
        }
        //client.getCharacter().getInventoryCache().safeDelete(item,1);

    }

    @HandlerAttribute(ID = ObjectUseMessage.MESSAGE_ID)
    public static void handleObjectUseMessage(WorldClient client, ObjectUseMessage message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(message.objectUID);
        if (item == null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DESTROY));
        } else if (!item.areConditionFilled(client.getCharacter())) {
            log.error("Wrond conditions");
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CRITERIONS));
        } else if (!item.getTemplate().use(client.getCharacter(), client.getCharacter(), item.getTemplateId() == 7010 ? item.getID() : client.getCharacter().getCell().getId())) {
            log.error("Item action criterias invalid");
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CRITERIONS));
        }

        //client.getCharacter().getInventoryCache().safeDelete(item,1);
    }

    @HandlerAttribute(ID = ObjectDeleteMessage.MESSAGE_ID)
    public static void HandleObjectDeleteMessage(WorldClient client, ObjectDeleteMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) || client.getMyExchange() != null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        client.getCharacter().getInventoryCache().safeDelete(client.getCharacter().getInventoryCache().find(Message.objectUID), Message.quantity);

    }

    @HandlerAttribute(ID = ObjectSetPositionMessage.MESSAGE_ID)
    public static void HandleObjectSetPositionMessage(WorldClient client, ObjectSetPositionMessage message) {
        synchronized (client.get$mutex()) {
            if(client.getCharacter() == null || client.getCharacter().getInventoryCache() == null || client.getMyExchange() != null){
                client.send(new BasicNoOperationMessage());
                return;
            }
            if (client.isGameAction(GameActionTypeEnum.FIGHT)
                    && client.getCharacter().getFight().getFightState() != FightState.STATE_PLACE) {
                client.send(new BasicNoOperationMessage());
                return;
            }

            client.getCharacter().getInventoryCache().moveItem(message.objectUID, CharacterInventoryPositionEnum.valueOf(message.position), message.quantity);
        }
    }

    @HandlerAttribute(ID = LivingObjectMessageRequestMessage.MESSAGE_ID)
    public static void HandleLivingObjectMessageRequestMessage(WorldClient client, LivingObjectMessageRequestMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) || client.getMyExchange() != null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(Message.livingObject);
        if (item == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        //int msgId, int timeStamp, String owner, int objectGenericId
        client.send(new LivingObjectMessageMessage(Message.msgId, (int) Instant.now().getEpochSecond(), client.getCharacter().getNickName(), item.getID()));
        client.send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = LivingObjectChangeSkinRequestMessage.MESSAGE_ID)
    public static void HandleLivingObjectChangeSkinRequestMessage(WorldClient client, LivingObjectChangeSkinRequestMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) || client.getMyExchange() != null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(Message.livingUID);
        if (item == null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        final ObjectEffectInteger obviXp = (ObjectEffectInteger) item.getEffect(974), obviSkin = (ObjectEffectInteger) item.getEffect(972);
        if (obviXp == null || obviSkin == null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (Message.skinId > ItemLivingObject.getLevelByObviXp(obviXp.value)) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && item.getApparrance() != 0) {
            client.getCharacter().getInventoryCache().removeApparence(item.getApparrance());
        }
        item.removeEffect(972);
        item.getEffects$Notify().add(((ObjectEffectInteger) obviSkin.Clone()).SetValue(Message.skinId));
        client.send(new ObjectModifiedMessage(item.getObjectItem()));
        if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && item.getApparrance() != 0) {
            client.getCharacter().getInventoryCache().addApparence(item.getApparrance());
            client.getCharacter().refreshEntitie();
        }
        client.getCharacter().send(new InventoryWeightMessage(client.getCharacter().getInventoryCache().getWeight(), client.getCharacter().getInventoryCache().getTotalWeight()));
        client.send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = ObjectFeedMessage.MESSAGE_ID)
    public static void handleObjectFeedMessage(WorldClient client, ObjectFeedMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) || client.getMyExchange() != null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(Message.objectUID), food = client.getCharacter().getInventoryCache().find(Message.foodUID);
        if (item == null || food == null || food.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (item instanceof PetsInventoryItem) {
            if (!((PetsInventoryItem) item).eat(client.getCharacter(), food)) {
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 53, new String[0]));
            } else {
                int newQua = food.getQuantity() - 1;
                if (newQua <= 0) {
                    client.getCharacter().getInventoryCache().removeItem(food);
                } else {
                    client.getCharacter().getInventoryCache().updateObjectquantity(food, newQua);
                }
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 32, new String[0]));
            }

        } else if (item.isLivingObject()) {
            ObjectEffectInteger obviXp = (ObjectEffectInteger) item.getEffect(974), obviType = (ObjectEffectInteger) item.getEffect(973), obviState = (ObjectEffectInteger) item.getEffect(971), obviSkin = (ObjectEffectInteger) item.getEffect(972), obviItem = (ObjectEffectInteger) item.getEffect(970);
            ObjectEffectDate obviTime = (ObjectEffectDate) item.getEffect(808);
            if (obviItem == null || obviType == null || obviType.value != food.getTemplate().getTypeId() || obviTime == null || obviXp == null || obviState == null || obviSkin == null) {
                client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
                return;
            }

            int newqua = food.getQuantity() - Message.foodQuantity;
            if (newqua < 0) {
                client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
                return;
            }
            final int xp = food.getTemplate().getLevel() / 2,
                    oldxp = obviXp.value,
                    state = obviState.value;
            if (newqua == 0) {
                client.getCharacter().getInventoryCache().removeItem(food);
            } else {
                client.getCharacter().getInventoryCache().updateObjectquantity(food, newqua);
            }
            item.removeEffect(974);
            item.getEffects$Notify().add(((ObjectEffectInteger) obviXp.Clone()).SetValue(oldxp + xp));
            if (state < 2) {
                item.removeEffect(971);
                item.getEffects$Notify().add(((ObjectEffectInteger) obviState.Clone()).SetValue(state + 1));
            }
            item.removeEffect(808);
            Calendar now = Calendar.getInstance();
            item.getEffects$Notify().add((new ObjectEffectDate(obviTime.actionId, now.get(Calendar.YEAR), (byte) now.get(Calendar.MONTH), (byte) now.get(Calendar.DAY_OF_MONTH), (byte) now.get(Calendar.HOUR), (byte) now.get(Calendar.MINUTE))));

            client.send(new ObjectModifiedMessage(item.getObjectItem()));
            client.getCharacter().send(new InventoryWeightMessage(client.getCharacter().getInventoryCache().getWeight(), client.getCharacter().getInventoryCache().getTotalWeight()));
        } else {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
        }
        client.send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = LivingObjectDissociateMessage.MESSAGE_ID)
    public static void HandleLivingObjectDissociateMessage(WorldClient client, LivingObjectDissociateMessage Message) {
        if (client.isGameAction(GameActionTypeEnum.FIGHT) || client.getMyExchange() != null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        final InventoryItem item = client.getCharacter().getInventoryCache().find(Message.livingUID);
        if (item == null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_UNEQUIP));
            return;
        }
        final ObjectEffectInteger obviXp = (ObjectEffectInteger) item.getEffect(974), obviType = (ObjectEffectInteger) item.getEffect(973), obviState = (ObjectEffectInteger) item.getEffect(971), obviSkin = (ObjectEffectInteger) item.getEffect(972), obviTemplate = (ObjectEffectInteger) item.getEffect(970);
        final ObjectEffectDate obviTime = (ObjectEffectDate) item.getEffect(808), exchangeTime = (ObjectEffectDate) item.getEffect(983);
        if (obviTemplate == null || obviXp == null || obviType == null || obviState == null || obviSkin == null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED/* && item.getTemplate().appearanceId != 0*/) {
            client.getCharacter().getInventoryCache().removeApparence(item.getApparrance());
        }
        client.getCharacter().getInventoryCache().tryCreateItem(obviTemplate.value, client.getCharacter(), 1, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), new ArrayList<ObjectEffect>() {
            {
                add(obviTemplate.Clone());
                add(obviXp.Clone());
                if(obviTime != null){
                    add(obviTime.Clone());
                }
                add(obviState.Clone());
                add(obviType.Clone());
                add(obviSkin.Clone());
                if (exchangeTime != null) {
                    add(exchangeTime.Clone());
                }
            }
        });

        item.removeEffect(974);
        item.removeEffect(973);
        item.removeEffect(971);
        item.removeEffect(972);
        item.removeEffect(808);
        item.removeEffect(983);
        item.removeEffect(970);

        client.send(new ObjectModifiedMessage(item.getObjectItem()));
        if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && item.getTemplate().getAppearanceId() != 0) {
            client.getCharacter().getInventoryCache().addApparence(item.getApparrance());
            client.getCharacter().refreshEntitie();
        }
        client.getCharacter().send(new InventoryWeightMessage(client.getCharacter().getInventoryCache().getWeight(), client.getCharacter().getInventoryCache().getTotalWeight()));
    }

}
