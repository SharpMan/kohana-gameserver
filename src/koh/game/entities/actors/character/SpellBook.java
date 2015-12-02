package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.spells.LearnableSpell;
import koh.game.entities.spells.SpellLevel;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.ShortcutBarEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellUpgradeFailureMessage;
import koh.protocol.messages.game.context.roleplay.spell.SpellUpgradeSuccessMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRefreshMessage;
import koh.protocol.messages.game.shortcut.ShortcutBarRemovedMessage;
import koh.protocol.types.game.data.items.SpellItem;
import koh.protocol.types.game.shortcut.Shortcut;
import koh.protocol.types.game.shortcut.ShortcutSpell;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class SpellBook {

    public class SpellInfo {

        public int id;
        public byte level;
        public byte position;

        public SpellInfo(int id, byte level, byte position) {
            this.id = id;
            this.level = level;
            this.position = position;
        }

        public SpellInfo(IoBuffer buf) {
            this.id = buf.getInt();
            this.level = buf.get();
            this.position = buf.get();
        }

        public SpellLevel getSpellLevel() {
            return DAO.getSpells().findSpell(id).spellLevels[level -1];
        }

        public SpellLevel getSpellLevel(byte Level) {
            return  DAO.getSpells().findSpell(id).spellLevels[Level - 1];
        }

        public SpellItem getSpellItem() {
            return new SpellItem((byte) 63, this.id, (byte) this.level);
        }

        public ShortcutSpell toShortcut() {
            return new ShortcutSpell(position, id);
        }

        public void serialize(IoBuffer buf) {
            buf.putInt(id);
            buf.put(level);
            buf.put(position);
        }

        public void totalClear() {
            try {
                id = 0;
                level = 0;
                position = 0;
                this.finalize();
            } catch (Throwable ex) {

            }
        }

    }

    private Map<Integer, SpellInfo> mySpells = Collections.synchronizedMap(new HashMap<Integer, SpellInfo>());

    public void totalClear() {
        try {
            for (SpellInfo s : mySpells.values()) {
                s.totalClear();
            }
            mySpells.clear();
            mySpells = null;
            this.finalize();
        } catch (Throwable ex) {

        }
    }

    public void generateLevelUpSpell(int Class, int Level) {

    }

    public static SpellBook generateForBreed(int Class, int Level) {
        SpellBook Book = new SpellBook();
        byte i = 0;
        Book.addSpell(0, (byte) 1, i++, null);
        for (LearnableSpell ls :  DAO.getSpells().findLearnableSpell(Class)) {
            if (ls.obtainLevel <= Level) {
                Book.addSpell(ls.spell, (byte) 1, i++, null);
            }
        }
        return Book;
    }

    public void removeSpellSlot(WorldClient Client, byte slot) {
        if (this.getByPos(slot) == null) {
            //Todo ShortcutErrorMessage
            Client.send(new BasicNoOperationMessage());
        } else {
            this.getByPos(slot).position = -1;
            Client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, slot));
        }
    }

    public boolean haventSpell() {
        return this.mySpells.isEmpty();
    }

    public void deserializeEffects(byte[] binary) {
        if (binary.length <= 0) {
            return;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            this.addSpell(new SpellInfo(buf));
        }
    }

    public byte[] serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(this.mySpells.size());
        this.mySpells.values().forEach(Spell -> Spell.serialize(buf));

        return buf.array();
    }

    public Shortcut[] toShortcuts() { //FIXME : Collectors.Arrays
        Shortcut[] array = new Shortcut[(int) this.mySpells.values().stream().filter(x -> x.position != -1).count()];
        int i = 0;
        for (SpellInfo sp : this.mySpells.values()) {
            if (sp.position == -1) {
                continue;
            }
            array[i] = new ShortcutSpell(sp.position, sp.id);
            i++;
        }
        return array;
    }

    public void addSpell(int spellId, byte level/* = 1*/, byte position/* = 25*/, WorldClient client) {
        if (!this.mySpells.containsKey(spellId)) {
            this.mySpells.put(spellId, new SpellInfo(spellId, level, position));
            if (client != null) {
                client.send(new SpellUpgradeSuccessMessage(spellId, (byte) level));
                client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(spellId).toShortcut()));
                client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 3, new String[]{String.valueOf(spellId)}));
            }

        }
    }

    public void removeSpell(Player owner, int id) {
        if (!this.mySpells.containsKey(id)) {
            return;
        }
        if (this.mySpells.get(id).level > 1) {
            owner.spellPoints += this.mySpells.get(id).level - 1;
            if (owner.client != null) {
                owner.client.send(new SpellUpgradeSuccessMessage(id, (byte) 0));
            }
        }
    }

    public boolean hasSpell(int SpellId) {
        return this.mySpells.containsKey(SpellId);
    }

    public void levelUpSepll(int spellId) {
        if (this.mySpells.containsKey(spellId)) {
            this.mySpells.get(spellId).level++;
        }
    }

    public SpellLevel getSpellLevel(int spellId) {
        if (this.mySpells.containsKey(spellId)) {
            return this.mySpells.get(spellId).getSpellLevel();
        }

        return null;
    }

    public void moveSpell(WorldClient client, int spellId, byte position) {
        this.mySpells.values().stream().filter(Spell -> Spell.position == position).forEach(Spell -> {
            Spell.position = -1;
            client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, position));
        });
        /*if (this.mySpells.get(SpellId).position != -1) {
         client.sendPacket(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(SpellId).position));
         }*/
        this.mySpells.get(spellId).position = position;
        client.sequenceMessage();
        client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(spellId).toShortcut()));
        client.sequenceMessage();
    }

    public void SwapShortcuts(WorldClient client, byte slot, byte newSlot) {
        SpellInfo shortcut1 = getByPos(slot);
        if (shortcut1 == null) {
            return;
        }
        SpellInfo shortcut2 = getByPos(newSlot);
        if (shortcut2 != null) {
            shortcut2.position = slot;
            client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, shortcut2.toShortcut()));
        } else {
            client.send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, (byte) slot));
        }
        shortcut1.position = newSlot;
        client.send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, shortcut1.toShortcut()));
    }

    public boolean BoostSpell(WorldClient client, int spellId, byte level) {
        if (this.mySpells.get(spellId) == null) {
            client.send(new SpellUpgradeFailureMessage());
            return false;
        } else {
            if (!this.CanBoostSpell(client, this.mySpells.get(spellId))) {
                client.send(new SpellUpgradeFailureMessage());
                return false;
            }
            while (level > this.mySpells.get(spellId).level) {
                if (!this.CanBoostSpell(client, this.mySpells.get(spellId))) {
                    break;
                }
                client.character.spellPoints -= this.mySpells.get(spellId).level;
                this.mySpells.get(spellId).level++;
            }
            client.send(new SpellUpgradeSuccessMessage(spellId, this.mySpells.get(spellId).level));
            return true;
        }
    }

    public boolean CanBoostSpell(WorldClient client, SpellInfo spell) {
        if (spell.level >= 6) {
            return false;
        } else if (client.character.spellPoints < (int) spell.level) {
            return false;
        } else {
            return spell.getSpellLevel((byte) (spell.level + 1)).minPlayerLevel <= client.character.level;
        }
    }

    public boolean isFreeSlot(int position) {
        return this.mySpells.values().stream().noneMatch((Spell) -> (Spell.position == position));
    }

    public byte getFreeSlot() {
        return (byte) (this.mySpells.values().stream().mapToInt(x -> x.position).max().getAsInt() + 1);
    }

    public SpellInfo getByPos(int Slot) {
        return this.mySpells.values().stream().filter(x -> x.position == Slot).findFirst().orElse(null);
    }

    public List<SpellLevel> getSpells() {
        return this.mySpells.values().stream().map(x -> x.getSpellLevel()).collect(Collectors.toList());
    }

    public void addSpell(SpellInfo Info) {
        this.mySpells.put(Info.id, Info);
    }

    public SpellItem[] toSpellItems() {
        SpellItem[] Array = new SpellItem[this.mySpells.size()];
        int i = 0;
        for (SpellInfo s : this.mySpells.values()) {
            Array[i] = s.getSpellItem();
            i++;
        }
        return Array;
    }

}
