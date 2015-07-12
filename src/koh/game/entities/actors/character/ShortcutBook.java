package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.client.enums.ShortcutType;
import koh.protocol.messages.game.shortcut.ShortcutBarRefreshMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRemovedMessage;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutObjectItem;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class ShortcutBook {

    public Map<Byte, PlayerShortcut> myShortcuts = Collections.synchronizedMap(new HashMap<Byte, PlayerShortcut>());

    public byte[] Serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(myShortcuts.size());
        myShortcuts.values().forEach(Spell -> Spell.Serialize(buf));

        return buf.array();
    }

    public void SwapShortcuts(WorldClient Client, byte slot, byte newSlot) {
        PlayerShortcut shortcut1 = myShortcuts.get(slot);
        if (shortcut1 == null) {
            return;
        }
        PlayerShortcut shortcut2 = myShortcuts.get(newSlot);
        myShortcuts.remove(slot);
        if (shortcut2 != null) {
            myShortcuts.remove(newSlot);
            shortcut2.Position = slot;
            this.Add(shortcut2);
            Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut2.toShortcut(Client.Character)));
        } else {
            Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, (byte) slot));
        }
        shortcut1.Position = newSlot;
        this.Add(shortcut1);
        Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut1.toShortcut(Client.Character)));
    }

    public Shortcut[] toShortcuts(Player p) { //FIXME : Collectors.Arrays
        Shortcut[] array = new Shortcut[this.myShortcuts.size()];
        int i = 0;
        for (PlayerShortcut sp : this.myShortcuts.values()) {
            if (sp.Position == -1) {
                continue;
            }
            array[i] = sp.toShortcut(p);
            i++;
        }
        return array;
    }

    public boolean CanAddShortcutItem(ShortcutObjectItem Item) {
        return !myShortcuts.values().stream().filter(x -> (x instanceof ItemShortcut) && ((ItemShortcut) x).ItemID == Item.itemUID).findAny().isPresent();
    }

    public static ShortcutBook Deserialize(byte[] binary) {
        ShortcutBook Book = new ShortcutBook();
        if (binary.length <= 0) {
            return Book;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            switch (buf.getInt()) {
                case ShortcutType.ShortcutItem:
                    Book.Add(new ItemShortcut(buf));
                    break;
                default:
                    throw new Error("type not supported");

            }
        }
        return Book;
    }

    public void Add(PlayerShortcut ps) {
        this.myShortcuts.put(ps.Position, ps);
    }

    public void totalClear() {
        try {
            for (PlayerShortcut ps : myShortcuts.values()) {
                ps.totalClear();
            }
            myShortcuts.clear();
            myShortcuts = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
