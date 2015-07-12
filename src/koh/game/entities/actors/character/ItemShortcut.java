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

    public int ItemID;

    public ItemShortcut(byte Pos, int Template) {
        super(Pos, ShortcutType.ShortcutItem);
        this.ItemID = Template;
    }

    @Override
    public Shortcut toShortcut(Player p) {
        return new ShortcutObjectItem(Position, p.InventoryCache.ItemsCache.get(this.ItemID).TemplateId, ItemID);
    }

    public ItemShortcut(IoBuffer buf) {
        super(buf.get(), ShortcutType.ShortcutItem);
        this.ItemID = buf.getInt();
    }

    @Override
    public void Serialize(IoBuffer buf) {
        super.Serialize(buf);
        buf.putInt(ItemID);
    }

}
