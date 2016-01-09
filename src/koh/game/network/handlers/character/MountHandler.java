package koh.game.network.handlers.character;

import java.time.Instant;
import java.util.ArrayList;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
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
        PlayerController.sendServerMessage(Client, "Non disponnible");
    }

    @HandlerAttribute(ID = MountRenameRequestMessage.M_ID)
    public static void HandleMountRenameRequestMessage(WorldClient Client, MountRenameRequestMessage Message) {
        if (Client.getCharacter().getMountInfo().mount != null && (int) Client.getCharacter().getMountInfo().mount.id == Message.mountId) {
            Client.getCharacter().getMountInfo().mount.name = Message.name;
            Client.getCharacter().getMountInfo().save();
            Client.send(new MountRenamedMessage(Message.name, Message.mountId));
        } else if (Client.getCharacter().getInventoryCache().getMount(Message.mountId) != null) {
            Client.getCharacter().getInventoryCache().getMount(Message.mountId).getMount().name = Message.name;
            Client.getCharacter().getInventoryCache().getMount(Message.mountId).save();
            Client.send(new MountRenamedMessage(Message.name, Message.mountId));
        }
    }

    @HandlerAttribute(ID = MountFeedRequestMessage.M_ID)
    public static void HandleMountFeedRequestMessage(WorldClient Client, MountFeedRequestMessage Message) {
        InventoryItem food = Client.getCharacter().getInventoryCache().find(Message.mountFoodUid);
        if (food == null || food.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            Client.send(new ObjectErrorMessage(ObjectErrorEnum.LIVING_OBJECT_REFUSED_FOOD));
            return;
        }
        int newQua = food.getQuantity() - Message.quantity;
        if (newQua <= 0) {
            Client.getCharacter().getInventoryCache().removeItem(food);
        } else {
            Client.getCharacter().getInventoryCache().updateObjectquantity(food, newQua);
        }
        Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 105, new String[0]));
    }

    @HandlerAttribute(ID = MountReleaseRequestMessage.M_ID)
    public static void HandleMountReleaseRequestMessage(WorldClient Client, MountReleaseRequestMessage Message) {
        PlayerController.sendServerMessage(Client, "La liberté des uns s'arrête là où commence celle des autres.");
    }

    @HandlerAttribute(ID = MountSterilizeRequestMessage.M_ID)
    public static void HandleMountSterilizeRequestMessage(WorldClient Client, MountSterilizeRequestMessage Message) {
        Client.send(new MountSterilizedMessage((int) Client.getCharacter().getMountInfo().mount.id));
    }

    @HandlerAttribute(ID = MountSetXpRatioRequestMessage.M_ID)
    public static void HandleMountSetXpRatioRequestMessage(WorldClient Client, MountSetXpRatioRequestMessage Message) {
        if (Message.xpRatio > 90) {
            throw new Error("You can't >= 90%");
        }
        if(Client.getCharacter().getMountInfo() == null){
            throw new NullPointerException("No mount present to set the ratio");
        }
        Client.getCharacter().getMountInfo().ratio = Message.xpRatio;
        Client.send(new MountXpRatioMessage(Client.getCharacter().getMountInfo().ratio));
    }

    @HandlerAttribute(ID = 5976)
    public static void HandleMountToggleRidingRequestMessage(WorldClient Client, MountToggleRidingRequestMessage Message) {
        if (Client.getCharacter().getMountInfo().mount == null) {
            throw new Error(Client.getCharacter().getNickName() + " try to ride NullableMount");
        }
        if (Client.getCharacter().getMountInfo().isToogled) {
            Client.getCharacter().getMountInfo().onGettingOff();
        } else {
            Client.getCharacter().getMountInfo().onRiding();
        }
        Client.getCharacter().getMountInfo().save();
    }

    @HandlerAttribute(ID = ExchangeHandleMountsStableMessage.ID)
    public static void HandleExchangeHandleMountsStableMessage(WorldClient Client, ExchangeHandleMountsStableMessage Message ) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            switch (Message.actionType) {
                case ExchangeHandleMountStableTypeEnum.EXCHANGE_UNCERTIF_TO_EQUIPED:
                    if (Client.getCharacter().getMountInfo().mount != null) {
                        PlayerController.sendServerMessage(Client, "You are already in a mount");
                        break;
                    }
                    InventoryItem dragodinde = Client.getCharacter().getInventoryCache().find(Message.ridesId[0]);
                    if (dragodinde == null) {
                        PlayerController.sendServerMessage(Client, "Nullable InventoryMount");
                        break;
                    }
                    if(!dragodinde.areConditionFilled(Client.getCharacter())){
                         Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 19, new String[0]));
                         break;
                    }
                    Client.getCharacter().getMountInfo().mount = ((MountInventoryItem) dragodinde).getMount();
                    Client.getCharacter().getMountInfo().entity = ((MountInventoryItem) dragodinde).getEntity();
                    Client.send(new MountRidingMessage(true));
                    Client.send(new MountSetMessage(Client.getCharacter().getMountInfo().mount));
                    Client.getCharacter().getInventoryCache().removeItem(dragodinde);
                    break;
                case ExchangeHandleMountStableTypeEnum.EXCHANGE_EQUIPED_CERTIF:
                    if (Client.getCharacter().getMountInfo().mount == null) {
                        PlayerController.sendServerMessage(Client, "Nullable mount");
                        break;
                    }

                    InventoryItem Item = InventoryItem.getInstance(DAO.getItems().nextItemId(), DAO.getMounts().find(Client.getCharacter().getMountInfo().mount.model).getScroolId(), 63, Client.getCharacter().getID(), 1, new ArrayList<ObjectEffect>() {
                        {
                            add(new ObjectEffectDuration(998, 37, (byte) 0, (byte) 0));
                            add(new ObjectEffectMount(995, (double) Instant.now().toEpochMilli(), Client.getCharacter().getMountInfo().mount.model, Client.getCharacter().getMountInfo().entity.animalID));
                            add(new ObjectEffectString(987, Client.getCharacter().getNickName()));
                        }
                    });
                    if (Client.getCharacter().getInventoryCache().add(Item, true)) {
                        Item.setNeedInsert(true);
                    }
                    Client.getCharacter().getMountInfo().onGettingOff();
                    Client.getCharacter().getMountInfo().mount = null;
                    Client.getCharacter().getMountInfo().entity = null;

                    Client.send(new MountRidingMessage(false));
                    Client.send(new MountUnSetMessage());
                    break;
                default:
                    PlayerController.sendServerMessage(Client, "Unsupported action: You can juste equip/unequip the mount");
            }
        }
    }

    @HandlerAttribute(ID = MountInformationRequestMessage.M_ID)
    public static void HandleMountInformationRequestMessage(WorldClient Client, MountInformationRequestMessage Message) {
        if (Client.getCharacter().getInventoryCache().getMount(Message.Id) == null) {
            return;
        } else {
            //Client.getCharacter().inventoryCache.getMount(Message.id).getEffects$Notify().add(new EffectInstanceString(new EffectInstance(0, 987, 0, "", 0, 0, 0, false, "C", 0, "", 0), "Melan"));
            Client.getCharacter().send(new MountDataMessage(Client.getCharacter().getInventoryCache().getMount(Message.Id).getMount()));
        }
    }

}
