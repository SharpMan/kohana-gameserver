package koh.game.exchange;

import koh.game.entities.actors.Player;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public abstract class Exchange {
    protected static final Logger logger = LogManager.getLogger(Exchange.class);

    protected boolean myEnd = false;

    public boolean exchangeFinish() {
        return this.myEnd;
    }
    //public int exchangeType;

    public abstract boolean transfertAllToInv(WorldClient client, InventoryItem[] items);

    public abstract boolean moveItems(WorldClient client, InventoryItem[] items, boolean add);

    public abstract boolean moveItem(WorldClient client, int itemID, int quantity);

    public abstract boolean moveKamas(WorldClient Client, int quantity);

    public abstract boolean buyItem(WorldClient client, int templateId, int quantity);

    public abstract boolean sellItem(WorldClient client, InventoryItem item, int quantity);

    public abstract boolean validate(WorldClient client);

    public abstract boolean finish();

    public boolean closeExchange() {
        return closeExchange(false);
    }

    public abstract boolean closeExchange(boolean success);

    public abstract void send(Message packet);

    public static InventoryItem[] getCharactersItems(Player character) {
        return character.getInventoryCache()
                .getItems()
                .filter(x -> !x.isLinked() && !x.isWorn())
                .toArray(InventoryItem[]::new);
    }

    public static InventoryItem[] getCharactersItems(Player character, int[] ids) {
        return character.getInventoryCache().getItems()
                .filter(x -> ArrayUtils.contains(ids, x.getID()) && !x.isLinked() && !x.isWorn())
                .toArray(InventoryItem[]::new);
    }

}
