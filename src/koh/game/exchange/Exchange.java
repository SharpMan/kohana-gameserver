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

    public abstract boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items);

    public abstract boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add);

    public abstract boolean MoveItem(WorldClient Client, int ItemID, int Quantity);

    public abstract boolean MoveKamas(WorldClient Client, int Quantity);

    public abstract boolean BuyItem(WorldClient Client, int TemplateId, int Quantity);

    public abstract boolean SellItem(WorldClient Client, InventoryItem Item, int Quantity);

    public abstract boolean Validate(WorldClient Client);

    public abstract boolean Finish();

    public boolean CloseExchange() {
        return CloseExchange(false);
    }

    public abstract boolean CloseExchange(boolean Success);

    public abstract void Send(Message Packet);

    public static InventoryItem[] CharactersItems(Player Character) {
        return Character.InventoryCache.ItemsCache.values().stream().filter(x -> !x.IsLinked() && !x.isEquiped()).toArray(InventoryItem[]::new);
    }

    public static InventoryItem[] CharactersItems(Player Character, int[] ids) {
        return Character.InventoryCache.ItemsCache.entrySet().stream().filter(x -> ArrayUtils.contains(ids, x.getKey()) && !x.getValue().IsLinked() && !x.getValue().isEquiped()).map(x -> x.getValue()).toArray(InventoryItem[]::new);
    }

}
