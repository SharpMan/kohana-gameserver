package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.ItemLivingObject;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDate;
import koh.game.entities.spells.EffectInstanceInteger;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.GameContextRefreshEntityLookMessage;
import koh.protocol.messages.game.inventory.InventoryWeightMessage;
import koh.protocol.messages.game.inventory.items.LivingObjectChangeSkinRequestMessage;
import koh.protocol.messages.game.inventory.items.LivingObjectDissociateMessage;
import koh.protocol.messages.game.inventory.items.LivingObjectMessageMessage;
import koh.protocol.messages.game.inventory.items.LivingObjectMessageRequestMessage;
import koh.protocol.messages.game.inventory.items.ObjectDeleteMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;
import koh.protocol.messages.game.inventory.items.ObjectFeedMessage;
import koh.protocol.messages.game.inventory.items.ObjectModifiedMessage;
import koh.protocol.messages.game.inventory.items.ObjectSetPositionMessage;

/**
 *
 * @author Neo-Craft
 */
public class InventoryHandler {

    @HandlerAttribute(ID = ObjectDeleteMessage.MESSAGE_ID)
    public static void HandleObjectDeleteMessage(WorldClient Client, ObjectDeleteMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.objectUID);
        if (Item == null || Message.quantity <= 0) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP));
            return;
        }
        if (Item.GetPosition() != 63) {
            Client.Character.InventoryCache.UnEquipItem(Item);
        }
        int newQua = Item.GetQuantity() - Message.quantity;
        if (newQua <= 0) {
            Client.Character.InventoryCache.RemoveItem(Item);
        } else {
            Client.Character.InventoryCache.UpdateObjectquantity(Item, newQua);
        }

    }

    @HandlerAttribute(ID = ObjectSetPositionMessage.MESSAGE_ID)
    public static void HandleObjectSetPositionMessage(WorldClient Client, ObjectSetPositionMessage Message) {
        //Todo if Figght
        Client.Character.InventoryCache.MoveItem(Message.objectUID, CharacterInventoryPositionEnum.valueOf(Message.position), Message.quantity);

    }

    @HandlerAttribute(ID = LivingObjectMessageRequestMessage.MESSAGE_ID)
    public static void HandleLivingObjectMessageRequestMessage(WorldClient Client, LivingObjectMessageRequestMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.livingObject);
        if (Item == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        //int msgId, int timeStamp, String owner, int objectGenericId
        Client.Send(new LivingObjectMessageMessage(Message.msgId, (int) Instant.now().getEpochSecond(), Client.Character.NickName, Item.ID));
        Client.Send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = LivingObjectChangeSkinRequestMessage.MESSAGE_ID)
    public static void HandleLivingObjectChangeSkinRequestMessage(WorldClient Client, LivingObjectChangeSkinRequestMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.livingUID);
        if (Item == null) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        EffectInstanceInteger obviXp = (EffectInstanceInteger) Item.GetEffect(974), obviSkin = (EffectInstanceInteger) Item.GetEffect(972);
        if (obviXp == null || obviSkin == null) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (Message.skinId > ItemLivingObject.GetLevelByObviXp(obviXp.value)) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && Item.Template().appearanceId != 0) {
            Client.Character.InventoryCache.RemoveApparence(Item.Apparrance());
        }
        Item.RemoveEffect(972);
        Item.getEffects().add(((EffectInstanceInteger) obviSkin.Clone()).SetValue(Message.skinId));
        Client.Send(new ObjectModifiedMessage(Item.ObjectItem()));
        if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && Item.Template().appearanceId != 0) {
            Client.Character.InventoryCache.AddApparence(Item.Apparrance());
            Client.Character.RefreshEntitie();
        }
        Client.Character.Send(new InventoryWeightMessage(Client.Character.InventoryCache.Weight(), Client.Character.InventoryCache.WeightTotal()));
        Client.Send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = ObjectFeedMessage.MESSAGE_ID)
    public static void HandleObjectFeedMessage(WorldClient Client, ObjectFeedMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.objectUID), Food = Client.Character.InventoryCache.ItemsCache.get(Message.foodUID);
        if (Item == null || Food == null || Food.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (Item instanceof PetsInventoryItem) {
            if (!((PetsInventoryItem) Item).Eat(Client.Character, Food)) {
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 53, new String[0]));
            } else {
                int newQua = Food.GetQuantity() - 1;
                if (newQua <= 0) {
                    Client.Character.InventoryCache.RemoveItem(Food);
                } else {
                    Client.Character.InventoryCache.UpdateObjectquantity(Food, newQua);
                }
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 32, new String[0]));
            }

        } else if (Item.isLivingObject()) {
            EffectInstanceInteger obviXp = (EffectInstanceInteger) Item.GetEffect(974), obviType = (EffectInstanceInteger) Item.GetEffect(973), obviState = (EffectInstanceInteger) Item.GetEffect(971), obviSkin = (EffectInstanceInteger) Item.GetEffect(972), obviItem = (EffectInstanceInteger) Item.GetEffect(970);
            EffectInstanceDate obviTime = (EffectInstanceDate) Item.GetEffect(808);
            if (obviItem == null || obviType == null || obviType.value != Food.Template().TypeId || obviTime == null || obviXp == null || obviState == null) {
                Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
                return;
            }

            int newqua = Food.GetQuantity() - Message.foodQuantity;
            if (newqua < 0) {
                Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
                return;
            }
            int xp = Food.Template().level / 2,
                    oldxp = obviXp.value,
                    state = obviState.value;
            if (newqua == 0) {
                Client.Character.InventoryCache.RemoveItem(Food);
            } else {
                Client.Character.InventoryCache.UpdateObjectquantity(Item, newqua);
            }
            Item.RemoveEffect(974);
            Item.getEffects().add(((EffectInstanceInteger) obviXp.Clone()).SetValue(oldxp + xp));
            //FIXME : if(state < 2) But useles...
            Item.RemoveEffect(971);
            Item.getEffects().add(((EffectInstanceInteger) obviState.Clone()).SetValue(state + 1));
            Item.RemoveEffect(808);
            Calendar now = Calendar.getInstance();
            Item.getEffects().add(((EffectInstanceDate) new EffectInstanceDate(obviTime, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.HOUR), now.get(Calendar.MINUTE))));

            Client.Send(new ObjectModifiedMessage(Item.ObjectItem()));
            Client.Character.Send(new InventoryWeightMessage(Client.Character.InventoryCache.Weight(), Client.Character.InventoryCache.WeightTotal()));
        } else {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
        }
        Client.Send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = LivingObjectDissociateMessage.MESSAGE_ID)
    public static void HandleLivingObjectDissociateMessage(WorldClient Client, LivingObjectDissociateMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.livingUID);
        if (Item == null) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        EffectInstanceInteger obviXp = (EffectInstanceInteger) Item.GetEffect(974), obviType = (EffectInstanceInteger) Item.GetEffect(973), obviState = (EffectInstanceInteger) Item.GetEffect(971), obviSkin = (EffectInstanceInteger) Item.GetEffect(972), obviTemplate = (EffectInstanceInteger) Item.GetEffect(970);
        EffectInstanceDate obviTime = (EffectInstanceDate) Item.GetEffect(808), exchangeTime = (EffectInstanceDate) Item.GetEffect(983);
        if (obviTemplate == null || obviXp == null || obviType == null || obviState == null || obviSkin == null || obviTime == null) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED/* && Item.Template().appearanceId != 0*/) {
            Client.Character.InventoryCache.RemoveApparence(Item.Apparrance());
        }
        Client.Character.InventoryCache.TryCreateItem(obviTemplate.value, Client.Character, 1, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), new ArrayList<EffectInstance>() {
            {
                add(obviTemplate.Clone());
                add(obviXp.Clone());
                add(obviTime.Clone());
                add(obviState.Clone());
                add(obviType.Clone());
                add(obviSkin.Clone());
                if (exchangeTime != null) {
                    add(exchangeTime.Clone());
                }
            }
        });

        Item.RemoveEffect(974);
        Item.RemoveEffect(973);
        Item.RemoveEffect(971);
        Item.RemoveEffect(972);
        Item.RemoveEffect(808);
        Item.RemoveEffect(983);
        Item.RemoveEffect(970);

        Client.Send(new ObjectModifiedMessage(Item.ObjectItem()));
        if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED && Item.Template().appearanceId != 0) {
            Client.Character.InventoryCache.AddApparence(Item.Apparrance());
            Client.Character.RefreshEntitie();
        }
        Client.Character.Send(new InventoryWeightMessage(Client.Character.InventoryCache.Weight(), Client.Character.InventoryCache.WeightTotal()));
    }

}
