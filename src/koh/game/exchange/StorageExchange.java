package koh.game.exchange;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeTypeEnum;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartedWithStorageMessage;
import koh.protocol.messages.game.inventory.storage.StorageInventoryContentMessage;
import koh.protocol.messages.game.inventory.storage.StorageKamasUpdateMessage;

/**
 *
 * @author Neo-Craft
 */
public class StorageExchange extends Exchange {

    private final WorldClient myClient;

    public StorageExchange(WorldClient Client) {
        this.myClient = Client;
        this.Send(new ExchangeStartedWithStorageMessage(ExchangeTypeEnum.STORAGE, 2147483647));
        this.Send(new StorageInventoryContentMessage(Client.getAccount().accountData.toObjectsItem(), Client.getAccount().accountData.kamas));
    }

    @Override
    public boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add) {
        InventoryItem NewItem = null;
        if (Add) {
            for (InventoryItem Item : Items) {
                NewItem = InventoryItem.getInstance(ItemTemplateDAOImpl.nextStorageId++, Item.templateId, 63, Client.getAccount().id, Item.getQuantity(), Item.effects);
                if (Client.getAccount().accountData.add(Client.character, NewItem, true)) {
                    NewItem.needInsert = true;
                }
                Client.character.inventoryCache.updateObjectquantity(Item, 0);
            }
        } else {
            for (InventoryItem Item : Items) {
                NewItem = InventoryItem.getInstance(ItemTemplateDAOImpl.nextId++, Item.templateId, 63, Client.character.ID, Item.getQuantity(), Item.effects);
                if (Client.character.inventoryCache.add(NewItem, true)) {
                    NewItem.needInsert = true;
                }
                Client.getAccount().accountData.updateObjectquantity(Client.character, Item, 0);
            }
        }
        return true;
    }

    @Override
    public boolean MoveItem(WorldClient Client, int ItemID, int Quantity) {
        if (Quantity == 0) {
            return false;
        } else if (Quantity <= 0) { //Remove from Bank
            InventoryItem BankItem = Client.getAccount().accountData.itemscache.get(ItemID);
            if (BankItem == null) {
                return false;
            }
            Client.getAccount().accountData.updateObjectquantity(Client.character, BankItem, BankItem.getQuantity() + Quantity);
            InventoryItem Item = InventoryItem.getInstance(ItemTemplateDAOImpl.nextId++, BankItem.templateId, 63, Client.character.ID, -Quantity, BankItem.effects);
            if (Client.character.inventoryCache.add(Item, true)) {
                Item.needInsert = true;
            }
        } else { //add In bank
            InventoryItem Item = Client.character.inventoryCache.itemsCache.get(ItemID);
            if (Item == null) {
                return false;
            }
            Client.character.inventoryCache.updateObjectquantity(Item, Item.getQuantity() - Quantity);
            InventoryItem NewItem = InventoryItem.getInstance(ItemTemplateDAOImpl.nextStorageId++, Item.templateId, 63, Client.getAccount().id, Quantity, Item.effects);
            if (Client.getAccount().accountData.add(Client.character, NewItem, true)) {
                NewItem.needInsert = true;
            }
        }
        return true;
    }

    @Override
    public boolean MoveKamas(WorldClient Client, int Quantity) {
        if (Quantity == 0) {
            return false;
        } else if (Quantity < 0) {
            if (Client.getAccount().accountData.kamas + Quantity < 0) {
                return false;
            }
            Client.getAccount().accountData.setBankKamas(Client.getAccount().accountData.kamas + Quantity);
            Client.send(new StorageKamasUpdateMessage(Client.getAccount().accountData.kamas));
            Client.character.inventoryCache.substractKamas(Quantity, false);
        } else {
            if (Client.character.kamas - Quantity < 0) {
                return false;
            }
            Client.getAccount().accountData.setBankKamas(Client.getAccount().accountData.kamas + Quantity);
            Client.send(new StorageKamasUpdateMessage(Client.getAccount().accountData.kamas));
            Client.character.inventoryCache.substractKamas(Quantity, false);
        }
        return true;
    }

    @Override
    public boolean BuyItem(WorldClient Client, int TemplateId, int Quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean SellItem(WorldClient Client, InventoryItem Item, int Quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean Validate(WorldClient Client) {
        return false;
    }

    @Override
    public boolean Finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean CloseExchange(boolean Success) {
        this.Finish();
        this.myClient.myExchange = null;
        this.myClient.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void Send(Message Packet) {
        this.myClient.send(Packet);
    }

    @Override
    public boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items) {
        return Client.myExchange.MoveItems(Client, Client.getAccount().accountData.itemscache.values().toArray(new InventoryItem[Client.getAccount().accountData.itemscache.size()]), false);
    }

}
