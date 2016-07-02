package koh.game.entities.actors.character.shortcut;

import koh.game.entities.actors.Player;
import koh.protocol.types.game.shortcut.Shortcut;
import lombok.ToString;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
@ToString
public class PlayerShortcut {

    public byte type;
    public byte position;

    public PlayerShortcut(byte Pos, byte Type) {
        this.position = Pos;
        this.type = Type;
    }

    public void serialize(IoBuffer buf) {
        buf.putInt(type);
        buf.put(position);
    }

    public Shortcut toShortcut(Player p) {
        return new Shortcut(position);
    }

    public void totalClear() {
        try {
            type = 0;
            position = 0;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
