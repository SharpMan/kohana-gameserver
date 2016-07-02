package koh.game.entities.actors.character.shortcut;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.shortcut.PlayerShortcut;
import koh.protocol.client.enums.ShortcutType;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutObjectItem;
import lombok.ToString;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
@ToString
public class ItemShortcut extends PlayerShortcut {

    public int itemID;

    public ItemShortcut(byte pos, int template) {
        super(pos, ShortcutType.SHORTCUT_ITEM);
        this.itemID = template;
    }

    @Override
    public Shortcut toShortcut(Player p) {
        return new ShortcutObjectItem(position, p.getInventoryCache().find(this.itemID).getTemplateId(), itemID);
    }

    public ItemShortcut(IoBuffer buf) {
        super(buf.get(), ShortcutType.SHORTCUT_ITEM);
        this.itemID = buf.getInt();
    }

    @Override
    public void serialize(IoBuffer buf) {
        super.serialize(buf);
        buf.putInt(itemID);
    }

}
