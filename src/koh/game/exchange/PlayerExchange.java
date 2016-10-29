package koh.game.exchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
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
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;
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

        logger.info("PlayerExchange launched : Player1={}" + " Player2={}" ,this.myClient1.getAccount().nickName, this.myClient2.getAccount().nickName);
    }

    public void Open() {
        this.myClient1.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient1.getCharacter().getID(), this.myClient1.getCharacter().getInventoryCache().getWeight(), this.myClient1.getCharacter().getInventoryCache().getTotalWeight(), this.myClient2.getCharacter().getID(), this.myClient2.getCharacter().getInventoryCache().getWeight(), this.myClient2.getCharacter().getInventoryCache().getTotalWeight()));
        this.myClient2.send(new ExchangeStartedWithPodsMessage(ExchangeTypeEnum.PLAYER_TRADE, this.myClient2.getCharacter().getID(), this.myClient2.getCharacter().getInventoryCache().getWeight(), this.myClient2.getCharacter().getInventoryCache().getTotalWeight(), this.myClient1.getCharacter().getID(), this.myClient1.getCharacter().getInventoryCache().getWeight(), this.myClient1.getCharacter().getInventoryCache().getTotalWeight()));
    }

    @Override
    public boolean moveItems(WorldClient client, InventoryItem[] items, boolean add) {
        if (items != null && items.length <= 0) {
            return false;
        }
        this.unValidateAll();

        if (add) {
            List<InventoryItem> modifiedObjects = null;

            for (InventoryItem item : items) {
                if (this.myItemsToTrade.get(client).containsKey(item.getID())) {
                    if (item.getQuantity() == this.myItemsToTrade.get(client).get(item.getID())) {
                        items = ArrayUtils.removeElement(items, item);
                    } else {
                        this.myItemsToTrade.get(client).put(item.getID(), item.getQuantity());
                        if (modifiedObjects == null) {
                            modifiedObjects = new ArrayList<>();
                        }
                        modifiedObjects.add(item);
                        items = ArrayUtils.removeElement(items, item);
                    }
                } else {
                    this.myItemsToTrade.get(client).put(item.getID(), item.getQuantity());
                }
            }
            if (modifiedObjects != null) {
                if (client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectsModifiedMessage(false, modifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                    this.myClient2.send(new ExchangeObjectsModifiedMessage(true, modifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                } else {
                    this.myClient2.send(new ExchangeObjectsModifiedMessage(false, modifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                    this.myClient1.send(new ExchangeObjectsModifiedMessage(true, modifiedObjects.stream().map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                }
                modifiedObjects.clear();
            }

            if (client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient2.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
            } else {
                this.myClient2.send(new ExchangeObjectsAddedMessage(false, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
                this.myClient1.send(new ExchangeObjectsAddedMessage(true, Arrays.stream(items).map(x -> x.getObjectItem()).toArray(ObjectItem[]::new)));
            }
        } else {
            if (client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectsRemovedMessage(false, this.myItemsToTrade.get(client).keySet().stream().mapToInt(x -> x).toArray()));
                this.myClient2.send(new ExchangeObjectsRemovedMessage(true, this.myItemsToTrade.get(client).keySet().stream().mapToInt(x -> x).toArray()));
            } else {
                this.myClient2.send(new ExchangeObjectsRemovedMessage(false, this.myItemsToTrade.get(client).keySet().stream().mapToInt(x -> x).toArray()));
                this.myClient1.send(new ExchangeObjectsRemovedMessage(true, this.myItemsToTrade.get(client).keySet().stream().mapToInt(x -> x).toArray()));
            }
            this.myItemsToTrade.get(client).clear();
        }
        return true;
    }

    @Override
    public synchronized boolean moveItem(WorldClient client, int itemID, int quantity) {
        final InventoryItem item = client.getCharacter().getInventoryCache().find(itemID);
        if (item == null) {
            return false;
        }
        if (item.isLinked() || item.isWorn()) {
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 345, new String[]{item.getTemplateId() + "", item.getID() + ""}));
            return false;
        }
        this.unValidateAll();

        if (!this.myItemsToTrade.get(client).containsKey(item.getID())) {  //add new item
            if (quantity <= 0) {
                return false;
            }
            if (quantity > item.getQuantity()) {
                quantity = item.getQuantity();
            }
            this.myItemsToTrade.get(client).put(item.getID(), quantity);

            if (client == this.myClient1) {
                this.myClient1.send(new ExchangeObjectAddedMessage(false, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                this.myClient2.send(new ExchangeObjectAddedMessage(true, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
            } else {
                this.myClient2.send(new ExchangeObjectAddedMessage(false, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                this.myClient1.send(new ExchangeObjectAddedMessage(true, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
            }
        } else {

            if (item.getQuantity() < (this.myItemsToTrade.get(client).get(item.getID()) + quantity) || (this.myItemsToTrade.get(client).get(item.getID()) + quantity) < 0) {
                return false;
            }

            if (this.myItemsToTrade.get(client).get(item.getID()) + quantity == 0) {
                this.myItemsToTrade.get(client).remove(item.getID());

                if (client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectRemovedMessage(false, item.getID()));
                    this.myClient2.send(new ExchangeObjectRemovedMessage(true, item.getID()));
                } else {
                    this.myClient2.send(new ExchangeObjectRemovedMessage(false, item.getID()));
                    this.myClient1.send(new ExchangeObjectRemovedMessage(true, item.getID()));
                }
            } else {
                this.myItemsToTrade.get(client).put(item.getID(), (this.myItemsToTrade.get(client).get(item.getID()) + quantity));

                if (client == this.myClient1) {
                    this.myClient1.send(new ExchangeObjectModifiedMessage(false, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                    this.myClient2.send(new ExchangeObjectModifiedMessage(true, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                } else {
                    this.myClient2.send(new ExchangeObjectModifiedMessage(false, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                    this.myClient1.send(new ExchangeObjectModifiedMessage(true, item.getObjectItem(this.myItemsToTrade.get(client).get(item.getID()))));
                }
            }
        }

        return true;
    }

    @Override
    public synchronized boolean moveKamas(WorldClient Client, int quantity) {
        logger.debug("PlayerExchange({} - {})::moveKamas : player={}" ,this.myClient1.getCharacter().getNickName(),this.myClient2.getCharacter().getNickName(), Client.getCharacter().getNickName());

        this.unValidateAll();

        if (quantity > Client.getCharacter().getKamas() || quantity < 0) {
            quantity = Client.getCharacter().getKamas();
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
    public boolean buyItem(WorldClient client, int templateId, int quantity) {
        return false;
    }

    @Override
    public boolean sellItem(WorldClient client, InventoryItem item, int quantity) {
        return false;
    }

    public synchronized void unValidateAll() {
        this.myValidate.put(this.myClient1, false);
        this.myValidate.put(this.myClient2, false);

        this.send(new ExchangeIsReadyMessage(this.myClient1.getCharacter().getID(), false));
        this.send(new ExchangeIsReadyMessage(this.myClient2.getCharacter().getID(), false));
    }

    @Override
    public synchronized boolean validate(WorldClient client) {
        this.myValidate.put(client, this.myValidate.get(client) == false);

        this.send(new ExchangeIsReadyMessage(client.getCharacter().getID(), this.myValidate.get(client)));
        if (this.myValidate.entrySet().stream().allMatch(x -> x.getValue())) {
            this.finish();

            try {
                this.myClient1.endGameAction(GameActionTypeEnum.EXCHANGE);
                this.myClient2.endGameAction(GameActionTypeEnum.EXCHANGE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            this.dispose();

            return true;
        }

        return false;
    }

    @Override
    public boolean finish() {
        if (this.myEnd) {
            return false;
        }

        logger.info("PlayerExchange(" + this.myClient1.getCharacter().getNickName() + " - " + this.myClient1.getCharacter().getNickName() + ")::finish()"
                + "\n          -- P1(items=" + Enumerable.join(this.myItemsToTrade.get(this.myClient1).entrySet().stream().mapToInt(y -> y.getKey()).toArray()) + " kamas=" + this.myKamasToTrade.get(this.myClient1) + ")"
                + "\n          -- P2(items=" + Enumerable.join(this.myItemsToTrade.get(this.myClient2).entrySet().stream().mapToInt(y -> y.getKey()).toArray()) + " kamas=" + this.myKamasToTrade.get(this.myClient2) + ")");

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient1).entrySet()) {
            final InventoryItem item = this.myClient1.getCharacter().getInventoryCache().find(ItemData.getKey());
            if (item == null) {
                logger.error(this.myClient1.getCharacter().getNickName() + " - " + this.myClient2.getCharacter().getNickName() + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= item.getQuantity()) {
                this.myClient1.getCharacter().getInventoryCache().changeOwner(item, this.myClient2.getCharacter());
                DAO.getItems().save(item, false, "character_items");
            } else {
                this.myClient1.getCharacter().getInventoryCache().updateObjectquantity(item, item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(item.getTemplateId(), this.myClient2.getCharacter(), ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), item.getEffectsCopy(), true);
            }
        }

        for (Entry<Integer, Integer> ItemData : this.myItemsToTrade.get(this.myClient2).entrySet()) {
            final InventoryItem item = this.myClient2.getCharacter().getInventoryCache().find(ItemData.getKey());
            if (item == null) {
                logger.error(this.myClient2.getCharacter().getNickName() + " - " + this.myClient1.getCharacter().getNickName() + " " + ItemData.getKey() + " item not Found");
                continue;
            }

            if (ItemData.getValue() >= item.getQuantity()) {
                this.myClient2.getCharacter().getInventoryCache().changeOwner(item, this.myClient1.getCharacter());
                DAO.getItems().save(item, false, "character_items");

            } else {
                this.myClient2.getCharacter().getInventoryCache().updateObjectquantity(item, item.getQuantity() - ItemData.getValue());
                CharacterInventory.tryCreateItem(item.getTemplateId(), this.myClient1.getCharacter(), ItemData.getValue(), CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), item.getEffectsCopy(), true);
            }
        }

        this.myClient1.getCharacter().getInventoryCache().substractKamas(this.myKamasToTrade.get(this.myClient1), false);
        this.myClient2.getCharacter().getInventoryCache().substractKamas(this.myKamasToTrade.get(this.myClient2), false);

        this.myClient1.getCharacter().getInventoryCache().addKamas(this.myKamasToTrade.get(this.myClient2), false);
        this.myClient2.getCharacter().getInventoryCache().addKamas(this.myKamasToTrade.get(this.myClient1), false);

        /*if(!this.myItemsToTrade.get(this.myClient1).isEmpty() || !this.myItemsToTrade.get(this.myClient2).isEmpty()){
            this.myClient1.getCharacter().getInventoryCache().save(false);
            this.myClient2.getCharacter().getInventoryCache().save(false);
        }*/

        return true;
    }

    @Override
    public synchronized boolean closeExchange(boolean success) {
        this.myClient1.setMyExchange(null);
        this.myClient2.setMyExchange(null);

        this.send(new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, success));

        this.myEnd = true;

        this.myClient1.endGameAction(GameActionTypeEnum.EXCHANGE);
        this.myClient2.endGameAction(GameActionTypeEnum.EXCHANGE);
        if (!success) {
            this.dispose();
        }

        return true;
    }

    @Override
    public void send(Message packet) {
        this.myClient1.send(packet);
        this.myClient2.send(packet);
    }

    public void dispose() {
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
    public boolean transfertAllToInv(WorldClient client, InventoryItem[] items) {
        return client.getMyExchange().moveItems(client, Exchange.getCharactersItems(client.getCharacter()), false);
    }

}
