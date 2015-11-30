package koh.game.exchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeIsReadyMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeLeaveMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectAddedMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectsAddedMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartedWithPodsMessage;
import koh.protocol.messages.game.inventory.items.ExchangeKamaModifiedMessage;
import koh.protocol.messages.game.inventory.items.ExchangeObjectModifiedMessage;
import koh.protocol.messages.game.inventory.items.ExchangeObjectRemovedMessage;
import koh.protocol.messages.game.inventory.items.ExchangeObjectsModifiedMessage;
import koh.protocol.messages.game.inventory.items.ExchangeObjectsRemovedMessage;
import koh.protocol.types.game.data.items.ObjectItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Neo-Craft
 */
public class PlayerExchange extends Exchange {

    private WorldClient myClient1, myClient2;
    private Map<WorldClient, Map<Integer, Integer>> myItemsToTrade = Collections.synchronizedMap(new HashMap<WorldClient, Map<Integer, Integer>>());
    private Map<WorldClient, Integer> myKamasToTrade = Collections.synchronizedMap(new HashMap<WorldClient, Integer>());
    private Map<WorldClient, Boolean> myValidate = Collections.synchronizedMap(new HashMap<WorldClient, Boolean>());

    public PlayerExchange(WorldClient Client1, WorldClient Client2) {
        this.myItemsToTrade.put(Client1, new HashMap<>());
        this.myItemsToTrade.put(Client2, new HashMap<>());
        this.myKamasToTrade.put(Client1, 0);
        this.myKamasToTrade.put(Client2, 0);
        this.myValidate.put(Client1, false);
        this.myValidate.put(Client2, false);

        this.myClient1 = Client1;
        this.myClient2 = Client2;

        Main.Logs().writeDebug("PlayerExchange launched : Player1=" + this.myClient1.getAccount().nickName + " Player2=" + this.myClient2.getAccount().nickName);
    }

    public void Open() {
        this.myClient1.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient1.character.ID, this.myClient1.character.inventoryCache.getWeight(), this.myClient1.character.inventoryCache.getTotalWeight(), this.myClient2.character.ID, this.myClient2.character.inventoryCache.getWeight(), this.myClient2.character.inventoryCache.getTotalWeight()));
        this.myClient2.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient2.character.ID, this.myClient2.character.inventoryCache.getWeight(), this.myClient2.character.inventoryCache.getTotalWeight(), this.myClient1.character.ID, this.myClient1.character.inventoryCache.getWeight(), this.myClient1.character.inventoryCache.getTotalWeight()));
    }

    @Override
    public boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add) {
        if (Items != null && Items.length <= 0) {
            return false;
        }
        this.UnValidateAll();

        if (Add) {
            List<InventoryItem> ModifiedObjects = null;

            for (InventoryItem Item : Items) {
                if (this.myItemsToTrade.get(Client).containsKey(Item.ID)) {
                    if (Item.getQuantity() == this.myItemsToTrade.get(Client).get(Item.ID)) {
                        Items = ArrayUtils.removeElement(Items, Item);
                    } else {
                        this.myItemsToTrade.get(Client).put(Item.ID, Item.getQuantity());
                        if (ModifiedObjects == null) {
                            ModifiedObjects = new ArrayList<>();
                        }
                        ModifiedObjects.add(Item);
                        Items = ArrayUtils.removeElement(Items, Item);
                    }
                } else {
                    this.myItemsToTrade.get(Client).put(Item.ID, Item.getQuantity());
                }
            }
            if (ModifiedObjects != null) {
                if (Client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectsModifiedMessage(false, ModifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                    this.myClient2.send(new ExchangeObjectsModifiedMessage(true, ModifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                } else {
                    this.myClient2.send(new ExchangeObjectsModifiedMessage(false, ModifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                    this.myClient1.send(new ExchangeObjectsModifiedMessage(true, ModifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                }
                ModifiedObjects.clear();
                ModifiedObjects = null;
            }

            if (Client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(Items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient2.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(Items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
            } else {
                this.myClient2.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(Items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient1.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(Items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
            }
        } else {
            if (Client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectsRemovedMessage(false, this.myItemsToTrade.get(Client).keySet().stream().mapToInt(x -> x).toArray()));
                this.myClient2.send(new ExchangeObjectsRemovedMessage(true, this.myItemsToTrade.get(Client).keySet().stream().mapToInt(x -> x).toArray()));
            } else {
                this.myClient2.send(new ExchangeObjectsRemovedMessage(false, this.myItemsToTrade.get(Client).keySet().stream().mapToInt(x -> x).toArray()));
                this.myClient1.send(new ExchangeObjectsRemovedMessage(true, this.myItemsToTrade.get(Client).keySet().stream().mapToInt(x -> x).toArray()));
            }
            this.myItemsToTrade.get(Client).clear();
        }
        return true;
    }

    @Override
    public synchronized boolean MoveItem(WorldClient Client, int ItemID, int Quantity) {
        InventoryItem Item = Client.character.inventoryCache.itemsCache.get(ItemID);
        if (Item == null) {
            return false;
        }
        if (Item.IsLinked() || Item.isEquiped()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 345, new String[]{Item.TemplateId + "", Item.ID + ""}));
            return false;
        }
        this.UnValidateAll();

        if (!this.myItemsToTrade.get(Client).containsKey(Item.ID)) {  //add new item
            if (Quantity <= 0) {
                return false;
            }
            if (Quantity > Item.getQuantity()) {
                Quantity = Item.getQuantity();
            }
            this.myItemsToTrade.get(Client).put(Item.ID, Quantity);

            if (Client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectAddedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                this.myClient2.send(new ExchangeObjectAddedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
            } else {
                this.myClient2.send(new ExchangeObjectAddedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                this.myClient1.send(new ExchangeObjectAddedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
            }
        } else {

            if (Item.getQuantity() < (this.myItemsToTrade.get(Client).get(Item.ID) + Quantity) || (this.myItemsToTrade.get(Client).get(Item.ID) + Quantity) < 0) {
                return false;
            }

            if (this.myItemsToTrade.get(Client).get(Item.ID) + Quantity == 0) {
                this.myItemsToTrade.get(Client).remove(Item.ID);

                if (Client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectRemovedMessage(false, Item.ID));
                    this.myClient2.send(new ExchangeObjectRemovedMessage(true, Item.ID));
                } else {
                    this.myClient2.send(new ExchangeObjectRemovedMessage(false, Item.ID));
                    this.myClient1.send(new ExchangeObjectRemovedMessage(true, Item.ID));
                }
            } else {
                this.myItemsToTrade.get(Client).put(Item.ID, (this.myItemsToTrade.get(Client).get(Item.ID) + Quantity));

                if (Client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectModifiedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                    this.myClient2.send(new ExchangeObjectModifiedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                } else {
                    this.myClient2.send(new ExchangeObjectModifiedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                    this.myClient1.send(new ExchangeObjectModifiedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.ID))));
                }
            }
        }

        return true;
    }

    @Override
    public synchronized boolean MoveKamas(WorldClient Client, int Quantity) {
        Main.Logs().writeDebug("PlayerExchange(" + this.myClient1.character.nickName + " - " + this.myClient2.character.nickName + ")::MoveKamas : player=" + Client.character.nickName);

        this.UnValidateAll();

        if (Quantity > Client.character.kamas || Quantity < 0) {
            Quantity = Client.character.kamas;
        }

        this.myKamasToTrade.put(Client, Quantity);

        if (Client == this.myClient1) {
            this.myClient1.send(new ExchangeKamaModifiedMessage(false, Quantity));
            this.myClient2.send(new ExchangeKamaModifiedMessage(true, Quantity));
        } else {
            this.myClient2.send(new ExchangeKamaModifiedMessage(false, Quantity));
            this.myClient1.send(new ExchangeKamaModifiedMessage(true, Quantity));
        }

        return true;
    }

    @Override
    public boolean BuyItem(WorldClient Client, int TemplateId, int Quantity) {
        return false;
    }

    @Override
    public boolean SellItem(WorldClient Client, InventoryItem Item, int Quantity) {
        return false;
    }

    public synchronized void UnValidateAll() {
        this.myValidate.put(this.myClient1, false);
        this.myValidate.put(this.myClient2, false);

        this.Send(new ExchangeIsReadyMessage(this.myClient1.character.ID, false));
        this.Send(new ExchangeIsReadyMessage(this.myClient2.character.ID, false));
    }

    @Override
    public synchronized boolean Validate(WorldClient Client) {
        this.myValidate.put(Client, this.myValidate.get(Client) == false);

        this.Send(new ExchangeIsReadyMessage(Client.character.ID, this.myValidate.get(Client)));
        if (this.myValidate.entrySet().stream().allMatch(x -> x.getValue())) {
            this.Finish();

            try {
                this.myClient1.endGameAction(GameActionTypeEnum.EXCHANGE);
                this.myClient2.endGameAction(GameActionTypeEnum.EXCHANGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            this.Dispose();

            return true;
        }

        return false;
    }

    @Override
    public boolean Finish() {
        if (this.myEnd) {
            return false;
        }

        Main.Logs().writeDebug("PlayerExchange(" + this.myClient1.character.nickName + " - " + this.myClient1.character.nickName + ")::Finish()"
                + "\n          -- P1(Items=" + StringUtils.join(this.myItemsToTrade.get(this.myClient1).entrySet().stream().mapToInt(y -> y.getKey()).toArray(), ",") + " kamas=" + this.myKamasToTrade.get(this.myClient1) + ")"
                + "\n          -- P2(Items=" + StringUtils.join(this.myItemsToTrade.get(this.myClient2).entrySet().stream().mapToInt(y -> y.getKey()).toArray(), ",") + " kamas=" + this.myKamasToTrade.get(this.myClient2) + ")");

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient1).entrySet()) {
            InventoryItem Item = this.myClient1.character.inventoryCache.itemsCache.get(ItemData.getKey());
            if (Item == null) {
                Main.Logs().writeError(this.myClient1.character.nickName + " - " + this.myClient2.character.nickName + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= Item.getQuantity()) {
                this.myClient1.character.inventoryCache.ChangeOwner(Item, this.myClient2.character);
            } else {
                this.myClient1.character.inventoryCache.updateObjectquantity(Item, Item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(Item.TemplateId, this.myClient2.character, ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy(), true);
            }
        }

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient2).entrySet()) {
            InventoryItem Item = this.myClient2.character.inventoryCache.itemsCache.get(ItemData.getKey());
            if (Item == null) {
                Main.Logs().writeError(this.myClient2.character.nickName + " - " + this.myClient1.character.nickName + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= Item.getQuantity()) {
                this.myClient2.character.inventoryCache.ChangeOwner(Item, this.myClient1.character);
            } else {
                this.myClient2.character.inventoryCache.updateObjectquantity(Item, Item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(Item.TemplateId, this.myClient1.character, ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy(), true);
            }
        }

        this.myClient1.character.inventoryCache.substractKamas(this.myKamasToTrade.get(this.myClient1), false);
        this.myClient2.character.inventoryCache.substractKamas(this.myKamasToTrade.get(this.myClient2), false);

        this.myClient1.character.inventoryCache.addKamas(this.myKamasToTrade.get(this.myClient2), false);
        this.myClient2.character.inventoryCache.addKamas(this.myKamasToTrade.get(this.myClient1), false);

        return true;
    }

    @Override
    public synchronized boolean CloseExchange(boolean Success) {
        this.myClient1.myExchange = null;
        this.myClient2.myExchange = null;

        this.Send(new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, Success));

        this.myEnd = true;

        this.myClient1.endGameAction(GameActionTypeEnum.EXCHANGE);
        this.myClient2.endGameAction(GameActionTypeEnum.EXCHANGE);
        System.out.print("action ended");

        if (!Success) {
            this.Dispose();
        }

        return true;
    }

    @Override
    public void Send(Message Packet) {
        this.myClient1.send(Packet);
        this.myClient2.send(Packet);
    }

    public void Dispose() {
        this.myItemsToTrade.clear();
        this.myKamasToTrade.clear();
        this.myValidate.clear();
        this.myClient1 = null;
        this.myKamasToTrade = null;
        this.myItemsToTrade = null;
        this.myValidate = null;
        this.myClient2 = null;
    }

    @Override
    public boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items) {
        return Client.myExchange.MoveItems(Client, Exchange.CharactersItems(Client.character), false);
    }

}
