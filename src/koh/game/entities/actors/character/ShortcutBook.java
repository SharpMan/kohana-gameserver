package koh.game.entities.actors.character;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public Map<Byte, PlayerShortcut> myShortcuts = new ConcurrentHashMap<Byte, PlayerShortcut>();

    public byte[] serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(myShortcuts.size());
        myShortcuts.values().forEach(Spell -> Spell.serialize(buf));

        return buf.array();
    }

    public void swapShortcuts(WorldClient Client, byte slot, byte newSlot) {
        PlayerShortcut shortcut1 = myShortcuts.get(slot);
        if (shortcut1 == null) {
            return;
        }
        PlayerShortcut shortcut2 = myShortcuts.get(newSlot);
        myShortcuts.remove(slot);
        if (shortcut2 != null) {
            myShortcuts.remove(newSlot);
            shortcut2.position = slot;
            this.add(shortcut2);
            Client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut2.toShortcut(Client.getCharacter())));
        } else {
            Client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, (byte) slot));
        }
        shortcut1.position = newSlot;
        this.add(shortcut1);
        Client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut1.toShortcut(Client.getCharacter())));
    }

    public Shortcut[] toShortcuts(Player p) {
        Shortcut[] array = new Shortcut[this.myShortcuts.size()];
        int i = 0;
        for (Map.Entry<Byte,PlayerShortcut> sp : this.myShortcuts.entrySet()) {
            if (sp.getValue().position == -1) {
                continue;
            }
            try {
                array[i] = sp.getValue().toShortcut(p);
            }
            catch(NullPointerException e){
                this.myShortcuts.remove(sp.getKey());
                continue;
            }
            i++;
        }
        return array;
    }

    public boolean canAddShortcutItem(ShortcutObjectItem Item) {
        return !myShortcuts.values().stream().filter(x -> (x instanceof ItemShortcut) && ((ItemShortcut) x).itemID == Item.itemUID).findAny().isPresent();
    }

    public static ShortcutBook deserialize(byte[] binary) {
        ShortcutBook Book = new ShortcutBook();
        if (binary.length <= 0) {
            return Book;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            switch (buf.getInt()) {
                case ShortcutType.ShortcutItem:
                    Book.add(new ItemShortcut(buf));
                    break;
                default:
                    throw new Error("type not supported");

            }
        }
        return Book;
    }

    public void add(PlayerShortcut ps) {
        this.myShortcuts.put(ps.position, ps);
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
