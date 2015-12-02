package koh.game.entities.actors.character;

import koh.game.entities.actors.Player;
import koh.protocol.client.enums.ShortcutType;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutObjectItem;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class ItemShortcut extends PlayerShortcut {

    public int itemID;

    public ItemShortcut(byte pos, int template) {
        super(pos, ShortcutType.ShortcutItem);
        this.itemID = template;
    }

    @Override
    public Shortcut toShortcut(Player p) {
        return new ShortcutObjectItem(position, p.inventoryCache.itemsCache.get(this.itemID).templateId, itemID);
    }

    public ItemShortcut(IoBuffer buf) {
        super(buf.get(), ShortcutType.ShortcutItem);
        this.itemID = buf.getInt();
    }

    @Override
    public void serialize(IoBuffer buf) {
        super.serialize(buf);
        buf.putInt(itemID);
    }

}
