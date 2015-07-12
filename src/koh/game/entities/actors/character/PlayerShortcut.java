package koh.game.entities.actors.character;

import java.util.ArrayList;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.ShortcutType;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutObjectItem;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class PlayerShortcut {

    public byte Type;
    public byte Position;

    public PlayerShortcut(byte Pos, byte Type) {
        this.Position = Pos;
        this.Type = Type;
    }

    public void Serialize(IoBuffer buf) {
        buf.putInt(Type);
        buf.put(Position);
    }

    public Shortcut toShortcut(Player p) {
        return new Shortcut(Position);
    }

    public void totalClear() {
        try {
            Type = 0;
            Position = 0;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
