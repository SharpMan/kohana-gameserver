package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.ArrayList;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.dao.sqlite.MountDAO;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.animal.MountInventoryItem;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ExchangeHandleMountStableTypeEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.mount.*;
import koh.protocol.messages.game.inventory.exchanges.ExchangeHandleMountsStableMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeRequestOnMountStockMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.effects.*;

/**
 *
 * @author Neo-Craft
 */
public class MountHandler {

    @HandlerAttribute(ID = ExchangeRequestOnMountStockMessage.M_ID)
    public static void HandleExchangeRequestOnMountStockMessage(WorldClient Client, ExchangeRequestOnMountStockMessage Message) {
        PlayerController.SendServerMessage(Client, "Non disponnible");
    }

    @HandlerAttribute(ID = MountRenameRequestMessage.M_ID)
    public static void HandleMountRenameRequestMessage(WorldClient Client, MountRenameRequestMessage Message) {
        if (Client.Character.MountInfo.Mount != null && (int) Client.Character.MountInfo.Mount.id == Message.mountId) {
            Client.Character.MountInfo.Mount.name = Message.name;
            Client.Character.MountInfo.Save();
            Client.Send(new MountRenamedMessage(Message.name, Message.mountId));
        } else if (Client.Character.InventoryCache.GetMount(Message.mountId) != null) {
            Client.Character.InventoryCache.GetMount(Message.mountId).Mount.name = Message.name;
            Client.Character.InventoryCache.GetMount(Message.mountId).Save();
            Client.Send(new MountRenamedMessage(Message.name, Message.mountId));
        }
    }

    @HandlerAttribute(ID = MountFeedRequestMessage.M_ID)
    public static void HandleMountFeedRequestMessage(WorldClient Client, MountFeedRequestMessage Message) {
        InventoryItem Food = Client.Character.InventoryCache.ItemsCache.get(Message.mountFoodUid);
        if (Food == null || Food.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        int newQua = Food.GetQuantity() - Message.quantity;
        if (newQua <= 0) {
            Client.Character.InventoryCache.RemoveItem(Food);
        } else {
            Client.Character.InventoryCache.UpdateObjectquantity(Food, newQua);
        }
        Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 105, new String[0]));
    }

    @HandlerAttribute(ID = MountReleaseRequestMessage.M_ID)
    public static void HandleMountReleaseRequestMessage(WorldClient Client, MountReleaseRequestMessage Message) {
        PlayerController.SendServerMessage(Client, "La liberté des uns s'arrête là où commence celle des autres.");
    }

    @HandlerAttribute(ID = MountSterilizeRequestMessage.M_ID)
    public static void HandleMountSterilizeRequestMessage(WorldClient Client, MountSterilizeRequestMessage Message) {
        Client.Send(new MountSterilizedMessage((int) Client.Character.MountInfo.Mount.id));
    }

    @HandlerAttribute(ID = MountSetXpRatioRequestMessage.M_ID)
    public static void HandleMountSetXpRatioRequestMessage(WorldClient Client, MountSetXpRatioRequestMessage Message) {
        if (Message.xpRatio > 90) {
            throw new Error("You can't >= 90%");
        }
        Client.Character.MountInfo.Ratio = Message.xpRatio;
        Client.Send(new MountXpRatioMessage(Client.Character.MountInfo.Ratio));
    }

    @HandlerAttribute(ID = 5976)
    public static void HandleMountToggleRidingRequestMessage(WorldClient Client, MountToggleRidingRequestMessage Message) {
        if (Client.Character.MountInfo.Mount == null) {
            throw new Error(Client.Character.NickName + " try to ride NullableMount");
        }
        if (Client.Character.MountInfo.isToogled) {
            Client.Character.MountInfo.OnGettingOff();
        } else {
            Client.Character.MountInfo.OnRiding();
        }
        Client.Character.MountInfo.Save();
    }

    @HandlerAttribute(ID = ExchangeHandleMountsStableMessage.ID)
    public static void HandleExchangeHandleMountsStableMessage(WorldClient Client, ExchangeHandleMountsStableMessage Message ) {
        if (Client.IsGameAction(GameActionTypeEnum.EXCHANGE)) {
            switch (Message.actionType) {
                case ExchangeHandleMountStableTypeEnum.EXCHANGE_UNCERTIF_TO_EQUIPED:
                    if (Client.Character.MountInfo.Mount != null) {
                        PlayerController.SendServerMessage(Client, "You are already in a mount");
                        break;
                    }
                    InventoryItem Dragodinde = Client.Character.InventoryCache.ItemsCache.get(Message.ridesId[0]);
                    if (Dragodinde == null) {
                        PlayerController.SendServerMessage(Client, "Nullable InventoryMount");
                        break;
                    }
                    if(!Dragodinde.AreConditionFilled(Client.Character)){
                         Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 19, new String[0]));
                         break;
                    }
                    Client.Character.MountInfo.Mount = ((MountInventoryItem) Dragodinde).Mount;
                    Client.Character.MountInfo.Entity = ((MountInventoryItem) Dragodinde).Entity;
                    Client.Send(new MountRidingMessage(true));
                    Client.Send(new MountSetMessage(Client.Character.MountInfo.Mount));
                    Client.Character.InventoryCache.RemoveItem(Dragodinde);
                    break;
                case ExchangeHandleMountStableTypeEnum.EXCHANGE_EQUIPED_CERTIF:
                    if (Client.Character.MountInfo.Mount == null) {
                        PlayerController.SendServerMessage(Client, "Nullable mount");
                        break;
                    }

                    InventoryItem Item = InventoryItem.Instance(ItemTemplateDAOImpl.nextId++, MountDAO.Model(Client.Character.MountInfo.Mount.model).ScroolId, 63, Client.Character.ID, 1, new ArrayList<ObjectEffect>() {
                        {
                            add(new ObjectEffectDuration(998, 37, (byte) 0, (byte) 0));
                            add(new ObjectEffectMount(995, (double) Instant.now().toEpochMilli(), Client.Character.MountInfo.Mount.model, Client.Character.MountInfo.Entity.AnimalID));
                            add(new ObjectEffectString(987, Client.Character.NickName));
                        }
                    });
                    if (Client.Character.InventoryCache.Add(Item, true)) {
                        Item.NeedInsert = true;
                    }
                    Client.Character.MountInfo.OnGettingOff();
                    Client.Character.MountInfo.Mount = null;
                    Client.Character.MountInfo.Entity = null;

                    Client.Send(new MountRidingMessage(false));
                    Client.Send(new MountUnSetMessage());
                    break;
                default:
                    PlayerController.SendServerMessage(Client, "Unsupported Action: You can juste equip/unequip the mount");
            }
        }
    }

    @HandlerAttribute(ID = MountInformationRequestMessage.M_ID)
    public static void HandleMountInformationRequestMessage(WorldClient Client, MountInformationRequestMessage Message) {
        if (Client.Character.InventoryCache.GetMount(Message.Id) == null) {
            return;
        } else {
            //Client.Character.InventoryCache.GetMount(Message.Id).getEffects().add(new EffectInstanceString(new EffectInstance(0, 987, 0, "", 0, 0, 0, false, "C", 0, "", 0), "Melan"));
            Client.Character.Send(new MountDataMessage(Client.Character.InventoryCache.GetMount(Message.Id).Mount));
        }
    }

}
