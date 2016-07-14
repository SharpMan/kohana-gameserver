package koh.game.entities.actors.character.shortcut;

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
        myShortcuts.values().forEach(spell -> spell.serialize(buf));

        return buf.array();
    }

    public void swapShortcuts(WorldClient Client, byte slot, byte newSlot) {
        final PlayerShortcut shortcut1 = myShortcuts.get(slot);
        if (shortcut1 == null) {
            return;
        }
       final PlayerShortcut shortcut2 = myShortcuts.get(newSlot);
        myShortcuts.remove(slot);
        if (shortcut2 != null) {
            myShortcuts.remove(newSlot);
            shortcut2.position = slot;
            this.add(shortcut2);
            Client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut2.toShortcut(Client.getCharacter())));
        } else {
            Client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, slot));
        }
        shortcut1.position = newSlot;
        this.add(shortcut1);
        Client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.GENERAL_SHORTCUT_BAR, shortcut1.toShortcut(Client.getCharacter())));
    }

    public Shortcut[] toShortcuts(Player p) {
        Shortcut[] array = new Shortcut[this.myShortcuts.size()];
        int i = 0;
        //Stack<Integer> nullPointers = null;
        for (Map.Entry<Byte,PlayerShortcut> sp : this.myShortcuts.entrySet()) {
            if (sp.getValue().position == -1) {
                continue;
            }
            try {
                array[i] = sp.getValue().toShortcut(p);
            }
            catch(NullPointerException e){
               /* if(nullPointers == null)
                    nullPointers = new Stack<>();
                nullPointers.add(i);*/
                //System.out.println(sp.toString());
                this.myShortcuts.remove(sp.getKey());
                continue;
            }
            i++;
        }
        /*if(nullPointers != null){
            for (Integer nullPointer : nullPointers) {
                array = ArrayUtils.remove(array,nullPointer);
            }
        }*/
        return array;
    }

    public boolean canAddShortcutItem(ShortcutObjectItem Item) {
        return !myShortcuts.values().stream().filter(x -> (x instanceof ItemShortcut) && ((ItemShortcut) x).itemID == Item.itemUID).findAny().isPresent();
    }

    public static ShortcutBook deserialize(byte[] binary) {

        final ShortcutBook book = new ShortcutBook();
        if (binary.length <= 0) {
            return book;
        }try {
            final IoBuffer buf = IoBuffer.wrap(binary);
            int len = buf.getInt();
            for (int i = 0; i < len; i++) {
                switch (buf.getInt()) {
                    case ShortcutType.SHORTCUT_ITEM:
                        book.add(new ItemShortcut(buf));
                        break;
                    case ShortcutType.SHORTCUT_PRESET:
                        book.add(new PresetShortcut(buf));
                        break;
                    default:
                        throw new Error("type not supported");

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return book;
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
