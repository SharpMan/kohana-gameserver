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
    public boolean moveItems(WorldClient client, InventoryItem[] items, boolean add) {
        InventoryItem newItem = null;
        if (add) {
            for (InventoryItem item : items) {
                newItem = InventoryItem.getInstance(DAO.getItems().nextItemStorageId(), item.getTemplateId(), 63, client.getAccount().id, item.getQuantity(), item.getEffects());
                if (client.getAccount().accountData.add(client.getCharacter(), newItem, true)) {
                    newItem.setNeedInsert(true);
                }
                client.getCharacter().getInventoryCache().updateObjectquantity(item, 0);
            }
        } else {
            for (InventoryItem Item : items) {
                newItem = InventoryItem.getInstance(DAO.getItems().nextItemId(), Item.getTemplateId(), 63, client.getCharacter().getID(), Item.getQuantity(), Item.getEffects());
                if (client.getCharacter().getInventoryCache().add(newItem, true)) {
                    newItem.setNeedInsert(true);
                }
                client.getAccount().accountData.updateObjectQuantity(client.getCharacter(), Item, 0);
            }
        }
        return true;
    }

    @Override
    public boolean moveItem(WorldClient client, int itemID, int quantity) {
        if (quantity == 0) {
            return false;
        } else if (quantity <= 0) { //Remove from Bank
            final InventoryItem bankItem = client.getAccount().accountData.itemscache.get(itemID);
            if (bankItem == null || quantity > bankItem.getQuantity() || bankItem.isNonExchangeable()) {
                return false;
            }
            client.getAccount().accountData.updateObjectQuantity(client.getCharacter(), bankItem, bankItem.getQuantity() + quantity);
            final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), bankItem.getTemplateId(), 63, client.getCharacter().getID(), -quantity, bankItem.getEffects());
            if (client.getCharacter().getInventoryCache().add(item, true)) {
                item.setNeedInsert(true);
            }
        } else { //add In bank
            final InventoryItem item = client.getCharacter().getInventoryCache().find(itemID);
            if (item == null || item.getQuantity() < quantity || item.isWorn() || item.isNonExchangeable()) {
                return false;
            }
            client.getCharacter().getInventoryCache().updateObjectquantity(item, item.getQuantity() - quantity);
            DAO.getItems().save(item, false, "character_items"); //TODO Insecure emplacement
            final InventoryItem newItem = InventoryItem.getInstance(DAO.getItems().nextItemStorageId(), item.getTemplateId(), 63, client.getAccount().id, quantity, item.getEffects());
            if (client.getAccount().accountData.add(client.getCharacter(), newItem, true)) {
                newItem.setNeedInsert(true);
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
            Client.getCharacter().getInventoryCache().substractKamas(quantity, false);
        } else {
            if (Client.getCharacter().getKamas() - quantity < 0) {
                return false;
            }
            Client.getAccount().accountData.setBankKamas(Client.getAccount().accountData.kamas + quantity);
            Client.send(new StorageKamasUpdateMessage(Client.getAccount().accountData.kamas));
            Client.getCharacter().getInventoryCache().substractKamas(quantity, false);
        }
        return true;
    }

    @Override
    public boolean buyItem(WorldClient client, int templateId, int quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sellItem(WorldClient client, InventoryItem item, int quantity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean validate(WorldClient client) {
        return false;
    }

    @Override
    public boolean finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean closeExchange(boolean success) {
        this.finish();
        this.myClient.setMyExchange(null);
        this.myClient.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void send(Message packet) {
        this.myClient.send(packet);
    }

    @Override
    public boolean transfertAllToInv(WorldClient client, InventoryItem[] items) {
        return client.getMyExchange().moveItems(client, client.getAccount().accountData.itemscache.values().toArray(new InventoryItem[client.getAccount().accountData.itemscache.size()]), false);
    }

}
