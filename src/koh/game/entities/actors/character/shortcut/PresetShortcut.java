package koh.game.entities.actors.character.shortcut;

import koh.game.entities.actors.Player;
import koh.protocol.client.enums.ShortcutType;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutObjectItem;
import koh.protocol.types.game.shortcut.ShortcutObjectPreset;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * Created by Melancholia on 6/30/16.
 */
public class PresetShortcut extends PlayerShortcut {

    public byte preset;

    public PresetShortcut(byte pos, byte template) {
        super(pos, ShortcutType.SHORTCUT_PRESET);
        this.preset = template;
    }

    @Override
    public Shortcut toShortcut(Player p) {
        return new ShortcutObjectPreset(position, preset);
    }

    public PresetShortcut(IoBuffer buf) {
        super(buf.get(), ShortcutType.SHORTCUT_PRESET);
        this.preset = buf.get();
    }

    @Override
    public void serialize(IoBuffer buf) {
        super.serialize(buf);
        buf.put(preset);
    }

}