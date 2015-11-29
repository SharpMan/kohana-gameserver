package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import koh.game.dao.mysql.SpellDAOImpl;
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

        public int Id;
        public byte Level;
        public byte Position;

        public SpellInfo(int Id, byte Level, byte Position) {
            this.Id = Id;
            this.Level = Level;
            this.Position = Position;
        }

        public SpellInfo(IoBuffer buf) {
            this.Id = buf.getInt();
            this.Level = buf.get();
            this.Position = buf.get();
        }

        public SpellLevel SpellLevel() {
            return SpellDAOImpl.spells.get(Id).spellLevels[Level -1];
        }

        public SpellLevel SpellLevel(byte Level) {
            return SpellDAOImpl.spells.get(Id).spellLevels[Level - 1];
        }

        public SpellItem GetSpellItem() {
            return new SpellItem((byte) 63, this.Id, (byte) this.Level);
        }

        public ShortcutSpell toShortcut() {
            return new ShortcutSpell(Position, Id);
        }

        public void Serialize(IoBuffer buf) {
            buf.putInt(Id);
            buf.put(Level);
            buf.put(Position);
        }

        public void totalClear() {
            try {
                Id = 0;
                Level = 0;
                Position = 0;
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

    public void GenerateLevelUpSpell(int Class, int Level) {

    }

    public static SpellBook GenerateForBreed(int Class, int Level) {
        SpellBook Book = new SpellBook();
        byte i = 0;
        Book.AddSpell(0, (byte) 1, i++, null);
        for (LearnableSpell ls : SpellDAOImpl.learnableSpells.get(Class)) {
            if (ls.ObtainLevel <= Level) {
                Book.AddSpell(ls.Spell, (byte) 1, i++, null);
            }
        }
        return Book;
    }

    public void RemoveSpellSlot(WorldClient Client, byte slot) {
        if (this.GetByPos(slot) == null) {
            //Todo ShortcutErrorMessage
            Client.Send(new BasicNoOperationMessage());
        } else {
            this.GetByPos(slot).Position = -1;
            Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, slot));
        }
    }

    public boolean HaventSpell() {
        return this.mySpells.isEmpty();
    }

    public void DeserializeEffects(byte[] binary) {
        if (binary.length <= 0) {
            return;
        }
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        for (int i = 0; i < len; i++) {
            this.AddSpell(new SpellInfo(buf));
        }
    }

    public byte[] Serialize() {
        IoBuffer buf = IoBuffer.allocate(1);
        buf.setAutoExpand(true);

        buf.putInt(this.mySpells.size());
        this.mySpells.values().forEach(Spell -> Spell.Serialize(buf));

        return buf.array();
    }

    public Shortcut[] toShortcuts() { //FIXME : Collectors.Arrays
        Shortcut[] array = new Shortcut[(int) this.mySpells.values().stream().filter(x -> x.Position != -1).count()];
        int i = 0;
        for (SpellInfo sp : this.mySpells.values()) {
            if (sp.Position == -1) {
                continue;
            }
            array[i] = new ShortcutSpell(sp.Position, sp.Id);
            i++;
        }
        return array;
    }

    public void AddSpell(int SpellId, byte Level/* = 1*/, byte Position/* = 25*/, WorldClient Client) {
        if (!this.mySpells.containsKey(SpellId)) {
            this.mySpells.put(SpellId, new SpellInfo(SpellId, Level, Position));
            if (Client != null) {
                Client.Send(new SpellUpgradeSuccessMessage(SpellId, (byte) Level));
                Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(SpellId).toShortcut()));
                Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 3, new String[]{String.valueOf(SpellId)}));
            }

        }
    }

    public void RemoveSpell(Player Owner, int id) {
        if (!this.mySpells.containsKey(id)) {
            return;
        }
        if (this.mySpells.get(id).Level > 1) {
            Owner.SpellPoints += this.mySpells.get(id).Level - 1;
            if (Owner.Client != null) {
                Owner.Client.Send(new SpellUpgradeSuccessMessage(id, (byte) 0));
            }
        }
    }

    public boolean HasSpell(int SpellId) {
        return this.mySpells.containsKey(SpellId);
    }

    public void LevelUpSepll(int SpellId) {
        if (this.mySpells.containsKey(SpellId)) {
            this.mySpells.get(SpellId).Level++;
        }
    }

    public SpellLevel GetSpellLevel(int SpellId) {
        if (this.mySpells.containsKey(SpellId)) {
            return this.mySpells.get(SpellId).SpellLevel();
        }

        return null;
    }

    public void MoveSpell(WorldClient Client, int SpellId, byte Position) {
        for (SpellInfo Spell : this.mySpells.values()) {
            if (Spell.Position == Position) {
                Spell.Position = -1;
                Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, Position));
            }
        }
        /*if (this.mySpells.get(SpellId).Position != -1) {
         Client.sendPacket(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(SpellId).Position));
         }*/
        this.mySpells.get(SpellId).Position = Position;
        Client.SequenceMessage();
        Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, this.mySpells.get(SpellId).toShortcut()));
        Client.SequenceMessage();
    }

    public void SwapShortcuts(WorldClient Client, byte slot, byte newSlot) {
        SpellInfo shortcut1 = GetByPos(slot);
        if (shortcut1 == null) {
            return;
        }
        SpellInfo shortcut2 = GetByPos(newSlot);
        if (shortcut2 != null) {
            shortcut2.Position = slot;
            Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, shortcut2.toShortcut()));
        } else {
            Client.Send(new ShortcutBarRemovedMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, (byte) slot));
        }
        shortcut1.Position = newSlot;
        Client.Send(new ShortcutBarRefreshMessage(ShortcutBarEnum.SPELL_SHORTCUT_BAR, shortcut1.toShortcut()));
    }

    public boolean BoostSpell(WorldClient Client, int SpellId, byte Level) {
        if (this.mySpells.get(SpellId) == null) {
            Client.Send(new SpellUpgradeFailureMessage());
            return false;
        } else {
            if (!this.CanBoostSpell(Client, this.mySpells.get(SpellId))) {
                Client.Send(new SpellUpgradeFailureMessage());
                return false;
            }
            while (Level > this.mySpells.get(SpellId).Level) {
                if (!this.CanBoostSpell(Client, this.mySpells.get(SpellId))) {
                    break;
                }
                Client.Character.SpellPoints -= this.mySpells.get(SpellId).Level;
                this.mySpells.get(SpellId).Level++;
            }
            Client.Send(new SpellUpgradeSuccessMessage(SpellId, this.mySpells.get(SpellId).Level));
            return true;
        }
    }

    public boolean CanBoostSpell(WorldClient Client, SpellInfo spell) {
        if (spell.Level >= 6) {
            return false;
        } else if (Client.Character.SpellPoints < (int) spell.Level) {
            return false;
        } else {
            return spell.SpellLevel((byte) (spell.Level + 1)).minPlayerLevel <= Client.Character.Level;
        }
    }

    public boolean isFreeSlot(int Position) {
        return this.mySpells.values().stream().noneMatch((Spell) -> (Spell.Position == Position));
    }

    public byte getFreeSlot() {
        return (byte) (this.mySpells.values().stream().mapToInt(x -> x.Position).max().getAsInt() + 1);
    }

    public SpellInfo GetByPos(int Slot) {
        try {
            return this.mySpells.values().stream().filter(x -> x.Position == Slot).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public List<SpellLevel> GetSpells() {
        return this.mySpells.values().stream().map(x -> x.SpellLevel()).collect(Collectors.toList());
    }

    public void AddSpell(SpellInfo Info) {
        this.mySpells.put(Info.Id, Info);
    }

    public SpellItem[] toSpellItems() {
        SpellItem[] Array = new SpellItem[this.mySpells.size()];
        int i = 0;
        for (SpellInfo s : this.mySpells.values()) {
            Array[i] = s.GetSpellItem();
            i++;
        }
        return Array;
    }

}
