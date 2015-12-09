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
import koh.game.dao.api.AccountDataDAO;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class PlayerExchange extends Exchange {

    private WorldClient myClient1, myClient2;
    private Map<WorldClient, Map<Integer, Integer>> myItemsToTrade = Collections.synchronizedMap(new HashMap<WorldClient, Map<Integer, Integer>>());
    private Map<WorldClient, Integer> myKamasToTrade = Collections.synchronizedMap(new HashMap<WorldClient, Integer>());
    private Map<WorldClient, Boolean> myValidate = Collections.synchronizedMap(new HashMap<WorldClient, Boolean>());

    private static final Logger logger = LogManager.getLogger(PlayerExchange.class);

    public PlayerExchange(WorldClient Client1, WorldClient Client2) {
        this.myItemsToTrade.put(Client1, new HashMap<>());
        this.myItemsToTrade.put(Client2, new HashMap<>());
        this.myKamasToTrade.put(Client1, 0);
        this.myKamasToTrade.put(Client2, 0);
        this.myValidate.put(Client1, false);
        this.myValidate.put(Client2, false);

        this.myClient1 = Client1;
        this.myClient2 = Client2;

        logger.debug("PlayerExchange launched : Player1={}" + " Player2={}" ,this.myClient1.getAccount().nickName, this.myClient2.getAccount().nickName);
    }

    public void Open() {
        this.myClient1.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient1.character.ID, this.myClient1.character.inventoryCache.getWeight(), this.myClient1.character.inventoryCache.getTotalWeight(), this.myClient2.character.ID, this.myClient2.character.inventoryCache.getWeight(), this.myClient2.character.inventoryCache.getTotalWeight()));
        this.myClient2.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient2.character.ID, this.myClient2.character.inventoryCache.getWeight(), this.myClient2.character.inventoryCache.getTotalWeight(), this.myClient1.character.ID, this.myClient1.character.inventoryCache.getWeight(), this.myClient1.character.inventoryCache.getTotalWeight()));
    }

    @Override
    public boolean moveItems(WorldClient Client, InventoryItem[] items, boolean add) {
        if (items != null && items.length <= 0) {
            return false;
        }
        this.UnValidateAll();

        if (add) {
            List<InventoryItem> ModifiedObjects = null;

            for (InventoryItem Item : items) {
                if (this.myItemsToTrade.get(Client).containsKey(Item.getID())) {
                    if (Item.getQuantity() == this.myItemsToTrade.get(Client).get(Item.getID())) {
                        items = ArrayUtils.removeElement(items, Item);
                    } else {
                        this.myItemsToTrade.get(Client).put(Item.getID(), Item.getQuantity());
                        if (ModifiedObjects == null) {
                            ModifiedObjects = new ArrayList<>();
                        }
                        ModifiedObjects.add(Item);
                        items = ArrayUtils.removeElement(items, Item);
                    }
                } else {
                    this.myItemsToTrade.get(Client).put(Item.getID(), Item.getQuantity());
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
                this.myClient1.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient2.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
            } else {
                this.myClient2.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient1.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
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
    public synchronized boolean moveItem(WorldClient Client, int itemID, int quantity) {
        InventoryItem Item = Client.character.inventoryCache.itemsCache.get(itemID);
        if (Item == null) {
            return false;
        }
        if (Item.isLinked() || Item.isEquiped()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 345, new String[]{Item.getTemplateId() + "", Item.getID() + ""}));
            return false;
        }
        this.UnValidateAll();

        if (!this.myItemsToTrade.get(Client).containsKey(Item.getID())) {  //add new item
            if (quantity <= 0) {
                return false;
            }
            if (quantity > Item.getQuantity()) {
                quantity = Item.getQuantity();
            }
            this.myItemsToTrade.get(Client).put(Item.getID(), quantity);

            if (Client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectAddedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                this.myClient2.send(new ExchangeObjectAddedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
            } else {
                this.myClient2.send(new ExchangeObjectAddedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                this.myClient1.send(new ExchangeObjectAddedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
            }
        } else {

            if (Item.getQuantity() < (this.myItemsToTrade.get(Client).get(Item.getID()) + quantity) || (this.myItemsToTrade.get(Client).get(Item.getID()) + quantity) < 0) {
                return false;
            }

            if (this.myItemsToTrade.get(Client).get(Item.getID()) + quantity == 0) {
                this.myItemsToTrade.get(Client).remove(Item.getID());

                if (Client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectRemovedMessage(false, Item.getID()));
                    this.myClient2.send(new ExchangeObjectRemovedMessage(true, Item.getID()));
                } else {
                    this.myClient2.send(new ExchangeObjectRemovedMessage(false, Item.getID()));
                    this.myClient1.send(new ExchangeObjectRemovedMessage(true, Item.getID()));
                }
            } else {
                this.myItemsToTrade.get(Client).put(Item.getID(), (this.myItemsToTrade.get(Client).get(Item.getID()) + quantity));

                if (Client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectModifiedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                    this.myClient2.send(new ExchangeObjectModifiedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                } else {
                    this.myClient2.send(new ExchangeObjectModifiedMessage(false, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                    this.myClient1.send(new ExchangeObjectModifiedMessage(true, Item.getObjectItem(this.myItemsToTrade.get(Client).get(Item.getID()))));
                }
            }
        }

        return true;
    }

    @Override
    public synchronized boolean moveKamas(WorldClient Client, int quantity) {
        logger.debug("PlayerExchange({} - {})::moveKamas : player={}" ,this.myClient1.character.nickName,this.myClient2.character.nickName, Client.character.nickName);

        this.UnValidateAll();

        if (quantity > Client.character.kamas || quantity < 0) {
            quantity = Client.character.kamas;
        }

        this.myKamasToTrade.put(Client, quantity);

        if (Client == this.myClient1) {
            this.myClient1.send(new ExchangeKamaModifiedMessage(false, quantity));
            this.myClient2.send(new ExchangeKamaModifiedMessage(true, quantity));
        } else {
            this.myClient2.send(new ExchangeKamaModifiedMessage(false, quantity));
            this.myClient1.send(new ExchangeKamaModifiedMessage(true, quantity));
        }

        return true;
    }

    @Override
    public boolean buyItem(WorldClient Client, int templateId, int quantity) {
        return false;
    }

    @Override
    public boolean sellItem(WorldClient Client, InventoryItem item, int quantity) {
        return false;
    }

    public synchronized void UnValidateAll() {
        this.myValidate.put(this.myClient1, false);
        this.myValidate.put(this.myClient2, false);

        this.send(new ExchangeIsReadyMessage(this.myClient1.character.ID, false));
        this.send(new ExchangeIsReadyMessage(this.myClient2.character.ID, false));
    }

    @Override
    public synchronized boolean validate(WorldClient Client) {
        this.myValidate.put(Client, this.myValidate.get(Client) == false);

        this.send(new ExchangeIsReadyMessage(Client.character.ID, this.myValidate.get(Client)));
        if (this.myValidate.entrySet().stream().allMatch(x -> x.getValue())) {
            this.finish();

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
    public boolean finish() {
        if (this.myEnd) {
            return false;
        }

        logger.debug("PlayerExchange(" + this.myClient1.character.nickName + " - " + this.myClient1.character.nickName + ")::finish()"
                + "\n          -- P1(items=" + StringUtils.join(this.myItemsToTrade.get(this.myClient1).entrySet().stream().mapToInt(y -> y.getKey()).toArray(), ",") + " kamas=" + this.myKamasToTrade.get(this.myClient1) + ")"
                + "\n          -- P2(items=" + StringUtils.join(this.myItemsToTrade.get(this.myClient2).entrySet().stream().mapToInt(y -> y.getKey()).toArray(), ",") + " kamas=" + this.myKamasToTrade.get(this.myClient2) + ")");

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient1).entrySet()) {
            InventoryItem Item = this.myClient1.character.inventoryCache.itemsCache.get(ItemData.getKey());
            if (Item == null) {
                logger.error(this.myClient1.character.nickName + " - " + this.myClient2.character.nickName + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= Item.getQuantity()) {
                this.myClient1.character.inventoryCache.ChangeOwner(Item, this.myClient2.character);
            } else {
                this.myClient1.character.inventoryCache.updateObjectquantity(Item, Item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(Item.getTemplateId(), this.myClient2.character, ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy(), true);
            }
        }

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient2).entrySet()) {
            InventoryItem Item = this.myClient2.character.inventoryCache.itemsCache.get(ItemData.getKey());
            if (Item == null) {
                logger.error(this.myClient2.character.nickName + " - " + this.myClient1.character.nickName + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= Item.getQuantity()) {
                this.myClient2.character.inventoryCache.ChangeOwner(Item, this.myClient1.character);
            } else {
                this.myClient2.character.inventoryCache.updateObjectquantity(Item, Item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(Item.getTemplateId(), this.myClient1.character, ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy(), true);
            }
        }

        this.myClient1.character.inventoryCache.substractKamas(this.myKamasToTrade.get(this.myClient1), false);
        this.myClient2.character.inventoryCache.substractKamas(this.myKamasToTrade.get(this.myClient2), false);

        this.myClient1.character.inventoryCache.addKamas(this.myKamasToTrade.get(this.myClient2), false);
        this.myClient2.character.inventoryCache.addKamas(this.myKamasToTrade.get(this.myClient1), false);

        return true;
    }

    @Override
    public synchronized boolean closeExchange(boolean Success) {
        this.myClient1.myExchange = null;
        this.myClient2.myExchange = null;

        this.send(new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, Success));

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
    public void send(Message Packet) {
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
    public boolean transfertAllToInv(WorldClient Client, InventoryItem[] items) {
        return Client.myExchange.moveItems(Client, Exchange.getCharactersItems(Client.character), false);
    }

}
