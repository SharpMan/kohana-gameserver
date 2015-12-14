package koh.game.exchange;

import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public abstract class Exchange {

    protected boolean myEnd = false;

    public boolean ExchangeFinish() {
        return this.myEnd;
    }
    public int ExchangeType;

    public abstract boolean transfertAllToInv(WorldClient Client, InventoryItem[] items);

    public abstract boolean moveItems(WorldClient Client, InventoryItem[] items, boolean add);

    public abstract boolean moveItem(WorldClient Client, int itemID, int quantity);

    public abstract boolean moveKamas(WorldClient Client, int quantity);

    public abstract boolean buyItem(WorldClient Client, int templateId, int quantity);

    public abstract boolean sellItem(WorldClient Client, InventoryItem item, int quantity);

    public abstract boolean validate(WorldClient Client);

    public abstract boolean finish();

    public boolean closeExchange() {
        return closeExchange(false);
    }

    public abstract boolean closeExchange(boolean Success);

    public abstract void send(Message Packet);

    public static InventoryItem[] getCharactersItems(Player Character) {
        return Character.inventoryCache.getItems().filter(x -> !x.isLinked() && !x.isEquiped()).toArray(InventoryItem[]::new);
    }

    public static InventoryItem[] getCharactersItems(Player Character, int[] ids) {
        return Character.inventoryCache.getItems().filter(x -> ArrayUtils.contains(ids, x.getID()) && !x.isLinked() && !x.isEquiped()).toArray(InventoryItem[]::new);
    }

}
