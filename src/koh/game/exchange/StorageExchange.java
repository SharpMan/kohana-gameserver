package koh.game.exchange;

import java.util.ArrayList;
import java.util.List;
import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.Npc;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
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
        this.Send(new StorageInventoryContentMessage(Client.getAccount().Data.toObjectsItem(), Client.getAccount().Data.Kamas));
    }

    @Override
    public boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add) {
        InventoryItem NewItem = null;
        if (Add) {
            for (InventoryItem Item : Items) {
                NewItem = InventoryItem.Instance(ItemDAO.NextStorageID++, Item.TemplateId, 63, Client.getAccount().ID, Item.GetQuantity(), Item.Effects);
                if (Client.getAccount().Data.Add(Client.Character, NewItem, true)) {
                    NewItem.NeedInsert = true;
                }
                Client.Character.InventoryCache.UpdateObjectquantity(Item, 0);
            }
        } else {
            for (InventoryItem Item : Items) {
                NewItem = InventoryItem.Instance(ItemDAO.NextID++, Item.TemplateId, 63, Client.Character.ID, Item.GetQuantity(), Item.Effects);
                if (Client.Character.InventoryCache.Add(NewItem, true)) {
                    NewItem.NeedInsert = true;
                }
                Client.getAccount().Data.UpdateObjectquantity(Client.Character, Item, 0);
            }
        }
        return true;
    }

    @Override
    public boolean MoveItem(WorldClient Client, int ItemID, int Quantity) {
        if (Quantity == 0) {
            return false;
        } else if (Quantity <= 0) { //Remove from Bank
            InventoryItem BankItem = Client.getAccount().Data.ItemsCache.get(ItemID);
            if (BankItem == null) {
                return false;
            }
            Client.getAccount().Data.UpdateObjectquantity(Client.Character, BankItem, BankItem.GetQuantity() + Quantity);
            InventoryItem Item = InventoryItem.Instance(ItemDAO.NextID++, BankItem.TemplateId, 63, Client.Character.ID, -Quantity, BankItem.Effects);
            if (Client.Character.InventoryCache.Add(Item, true)) {
                Item.NeedInsert = true;
            }
        } else { //Add In bank
            InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(ItemID);
            if (Item == null) {
                return false;
            }
            Client.Character.InventoryCache.UpdateObjectquantity(Item, Item.GetQuantity() - Quantity);
            InventoryItem NewItem = InventoryItem.Instance(ItemDAO.NextStorageID++, Item.TemplateId, 63, Client.getAccount().ID, Quantity, Item.Effects);
            if (Client.getAccount().Data.Add(Client.Character, NewItem, true)) {
                NewItem.NeedInsert = true;
            }
        }
        return true;
    }

    @Override
    public boolean MoveKamas(WorldClient Client, int Quantity) {
        if (Quantity == 0) {
            return false;
        } else if (Quantity < 0) {
            if (Client.getAccount().Data.Kamas + Quantity < 0) {
                return false;
            }
            Client.getAccount().Data.SetBankKamas(Client.getAccount().Data.Kamas + Quantity);
            Client.Send(new StorageKamasUpdateMessage(Client.getAccount().Data.Kamas));
            Client.Character.InventoryCache.SubstractKamas(Quantity, false);
        } else {
            if (Client.Character.Kamas - Quantity < 0) {
                return false;
            }
            Client.getAccount().Data.SetBankKamas(Client.getAccount().Data.Kamas + Quantity);
            Client.Send(new StorageKamasUpdateMessage(Client.getAccount().Data.Kamas));
            Client.Character.InventoryCache.SubstractKamas(Quantity, false);
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
        this.myClient.Send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.EndGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void Send(Message Packet) {
        this.myClient.Send(Packet);
    }

    @Override
    public boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items) {
        return Client.myExchange.MoveItems(Client, Client.getAccount().Data.ItemsCache.values().toArray(new InventoryItem[Client.getAccount().Data.ItemsCache.size()]), false);
    }

}
