package koh.game.dao.api;

import koh.game.entities.item.InventoryItem;
import koh.game.entities.spells.EffectInstance;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.EffectInstanceInteger;
import koh.patterns.services.api.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

import java.util.Map;

/**
 *
 * @author Neo-Craft
 */
public abstract class ItemDAO implements Service {

    public abstract int nextItemId();
    public abstract int nextItemStorageId();

    public abstract void initInventoryCache(int player, Map<Integer, InventoryItem> cache, String table);
    public abstract boolean create(InventoryItem item, boolean clear, String table);
    public abstract boolean save(InventoryItem item, boolean clear, String table);
    public abstract boolean delete(InventoryItem item, String table);

}
