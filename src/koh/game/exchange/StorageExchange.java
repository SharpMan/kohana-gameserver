package koh.game.exchange;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
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
        this.send(new ExchangeStartedWithStorageMessage(ExchangeTypeEnum.STORAGE, 2147483647));
        this.send(new StorageInventoryContentMessage(Client.getAccount().accountData.toObjectsItem(), Client.getAccount().accountData.kamas));
    }

    @Override
    public boolean moveItems(WorldClient Client, InventoryItem[] items, boolean add) {
        InventoryItem newItem = null;
        if (add) {
            for (InventoryItem Item : items) {
                newItem = InventoryItem.getInstance(DAO.getItems().nextItemStorageId(), Item.getTemplateId(), 63, Client.getAccount().id, Item.getQuantity(), Item.getEffects());
                if (Client.getAccount().accountData.add(Client.character, newItem, true)) {
                    newItem.setNeedInsert(true);
                }
                Client.character.getInventoryCache().updateObjectquantity(Item, 0);
            }
        } else {
            for (InventoryItem Item : items) {
                newItem = InventoryItem.getInstance(DAO.getItems().nextItemId(), Item.getTemplateId(), 63, Client.character.ID, Item.getQuantity(), Item.getEffects());
                if (Client.character.getInventoryCache().add(newItem, true)) {
                    newItem.setNeedInsert(true);
                }
                Client.getAccount().accountData.updateObjectQuantity(Client.character, Item, 0);
            }
        }
        return true;
    }

    @Override
    public boolean moveItem(WorldClient client, int itemID, int quantity) {
        if (quantity == 0) {
            return false;
        } else if (quantity <= 0) { //Remove from Bank
            InventoryItem BankItem = client.getAccount().accountData.itemscache.get(itemID);
            if (BankItem == null) {
                return false;
            }
            client.getAccount().accountData.updateObjectQuantity(client.character, BankItem, BankItem.getQuantity() + quantity);
            InventoryItem Item = InventoryItem.getInstance(DAO.getItems().nextItemId(), BankItem.getTemplateId(), 63, client.character.ID, -quantity, BankItem.getEffects());
            if (client.character.getInventoryCache().add(Item, true)) {
                Item.setNeedInsert(true);
            }
        } else { //add In bank
            InventoryItem Item = client.character.getInventoryCache().find(itemID);
            if (Item == null) {
                return false;
            }
            client.character.getInventoryCache().updateObjectquantity(Item, Item.getQuantity() - quantity);
            InventoryItem NewItem = InventoryItem.getInstance(DAO.getItems().nextItemStorageId(), Item.getTemplateId(), 63, client.getAccount().id, quantity, Item.getEffects());
            if (client.getAccount().accountData.add(client.character, NewItem, true)) {
                NewItem.setNeedInsert(true);
            }
        }
        return true;
    }

    @Override
    public boolean moveKamas(WorldClient Client, int quantity) {
        if (quantity == 0) {
            return false;
        } else if (quantity < 0) {
            if (Client.getAccount().accountData.kamas + quantity < 0) {
                return false;
            }
            Client.getAccount().accountData.setBankKamas(Client.getAccount().accountData.kamas + quantity);
            Client.send(new StorageKamasUpdateMessage(Client.getAccount().accountData.kamas));
            Client.character.getInventoryCache().substractKamas(quantity, false);
        } else {
            if (Client.character.getKamas() - quantity < 0) {
                return false;
            }
            Client.getAccount().accountData.setBankKamas(Client.getAccount().accountData.kamas + quantity);
            Client.send(new StorageKamasUpdateMessage(Client.getAccount().accountData.kamas));
            Client.character.getInventoryCache().substractKamas(quantity, false);
        }
        return true;
    }

    @Override
    public boolean buyItem(WorldClient Client, int templateId, int quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sellItem(WorldClient Client, InventoryItem item, int quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean validate(WorldClient Client) {
        return false;
    }

    @Override
    public boolean finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean closeExchange(boolean Success) {
        this.finish();
        this.myClient.myExchange = null;
        this.myClient.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void send(Message Packet) {
        this.myClient.send(Packet);
    }

    @Override
    public boolean transfertAllToInv(WorldClient Client, InventoryItem[] items) {
        return Client.myExchange.moveItems(Client, Client.getAccount().accountData.itemscache.values().toArray(new InventoryItem[Client.getAccount().accountData.itemscache.size()]), false);
    }

}
