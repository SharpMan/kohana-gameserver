package koh.game.entities.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import koh.game.Main;
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.item.animal.MountInventoryItem;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.game.entities.spells.*;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.ObjectItem;
import koh.protocol.types.game.data.items.effects.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class InventoryItem {

    public int ID;
    public int TemplateId;
    private int Position;
    private int Owner;
    private int Quantity;
    public List<EffectInstance> Effects;
    public boolean NeedInsert, NeedRemove;
    public List<String> ColumsToUpdate = null;

    public InventoryItem() {

    }

    private GenericStats myStats;

    public static InventoryItem Instance(int ID, int TemplateId, int Position, int Owner, int Quantity, List<EffectInstance> Effects) {
        if (ItemDAO.Cache.get(TemplateId).GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
            if (Effects.stream().anyMatch(x -> x.effectId == 995)) {
                Main.Logs().writeDebug("Contains Aninal");
                return new PetsInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, false);
            } else {
                Main.Logs().writeDebug("Create Animal");
                return new PetsInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, true);
            }
        } else if (ItemDAO.Cache.get(TemplateId).TypeId == 97) {
            if (Effects.stream().anyMatch(x -> x.effectId == 995)) {
                Main.Logs().writeDebug("Contains AninalM");
                return new MountInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, false);
            } else {
                Main.Logs().writeDebug("Create AnimalM");
                return new MountInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, true);
            }

        } else {
            return new InventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects);
        }
    }

    public InventoryItem(int ID, int TemplateId, int Position, int Owner, int Quantity, List<EffectInstance> Effects) {
        this.ID = ID;
        this.TemplateId = TemplateId;
        this.Position = Position;
        this.Owner = Owner;
        this.Quantity = Quantity;
        this.Effects = Effects;
    }

    public ObjectItem ObjectItem(int WithQuantity) {
        return new ObjectItem(this.Position, this.TemplateId, ObjectEffects(Effects.stream().filter(x -> x.visibleInTooltip).collect(Collectors.toList())), this.ID, WithQuantity);
    }

    public ObjectItem ObjectItem() {
        return new ObjectItem(this.Position, this.TemplateId, ObjectEffects(Effects.stream().filter(x -> x.visibleInTooltip).collect(Collectors.toList())), this.ID, this.Quantity);
    }

    public ItemSuperTypeEnum GetSuperType() {
        return ItemSuperTypeEnum.valueOf(ItemDAO.SuperTypes.get(Template().TypeId));
    }

    public boolean isEquiped() {
        return this.Position != 63;
    }

    public boolean IsLinked() { //928 = Lié 983 = Non echangeable
        return this.hasEffect(982) || this.hasEffect(983) || this.GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_QUEST || this.IsTokenItem();
    }

    public boolean isLivingObject() {
        return this.Template().TypeId == 113;
    }

    public short Apparrance() {
        EffectInstanceInteger effect = (EffectInstanceInteger) this.GetEffect(972);
        if (effect == null) {
            return this.Template().appearanceId;
        } else {
            EffectInstanceInteger type = (EffectInstanceInteger) this.GetEffect(970);
            if (type == null) {
                return this.Template().appearanceId;
            }
            return (short) ItemLivingObject.GetObviAppearanceBySkinId(effect.value, type.value);
        }
    }

    public int GetPosition() {
        return Position;
    }

    public void SetPosition(int i) {
        this.Position = i;
        this.NotifiedColumn("position");
    }

    public int GetQuantity() {
        return Quantity;
    }

    public void SetQuantity(int i) {
        this.Quantity = i;
        this.NotifiedColumn("stack");
    }

    public int GetOwner() {
        return Owner;
    }

    public void SetOwner(int i) {
        this.Owner = i;
        this.NotifiedColumn("owner");
    }

    public void NotifiedColumn(String C) {
        if (this.ColumsToUpdate == null) {
            this.ColumsToUpdate = new ArrayList<>();
        }
        if (!this.ColumsToUpdate.contains(C)) {
            this.ColumsToUpdate.add(C);
        }
    }

    public boolean hasEffect(int id) {
        return this.Effects.stream().anyMatch(x -> x.effectId == id);
    }

    public EffectInstance GetEffect(int id) {
        try {
            return this.Effects.stream().filter(x -> x.effectId == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public ItemTemplate Template() {
        return ItemDAO.Cache.get(TemplateId);
    }

    public CharacterInventoryPositionEnum Slot() {
        return CharacterInventoryPositionEnum.valueOf((byte) this.Position);
    }

    public void RemoveEffect(int id) {
        if (this.Effects.removeIf(x -> x.effectId == id)) {
            this.NotifiedColumn("effects");
        }
    }

    public List<EffectInstance> getEffects() {
        this.NotifiedColumn("effects");
        return Effects;
    }

    public List<EffectInstance> getEffectsCopy() {
        List<EffectInstance> effects = new ArrayList<>();
        this.Effects.stream().forEach((e) -> { //TODO: Parralel Stream
            effects.add(e.Clone());
        });
        return effects;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int Weight() {
        return this.Template().realWeight * this.Quantity;
    }

    public void SetPosition(CharacterInventoryPositionEnum Slot) {
        this.Position = Slot.value();
        this.NotifiedColumn("position");
    }

    public boolean Equals(Collection<EffectInstance> Effects) {
        EffectInstance Get = null;
        if (Effects.size() != this.Effects.size()) {
            return false;
        }
        for (EffectInstance e : Effects) {
            Get = this.GetEffect(e.effectId);
            if (Get == null || !Get.equals(e)) {
                return false;
            }
        }
        return true;
    }

    public static List<EffectInstance> DeserializeEffects(byte[] binary) {
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        List<EffectInstance> Effects = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            switch (buf.get()) {
                case 1:
                    Effects.add(new EffectInstance(buf));
                    break;
                case 2:
                    Effects.add(new EffectInstanceInteger(buf));
                    break;
                case 3:
                    Effects.add(new EffectInstanceDice(buf));
                    break;
                case 4:
                    Effects.add(new EffectInstanceDate(buf));
                    break;
                case 5:
                    Effects.add(new EffectInstanceMinMax(buf));
                    break;
                case 6:
                    Effects.add(new EffectInstanceCreature(buf));
                    break;
                case 7:
                    Effects.add(new EffectInstanceDuration(buf));
                    break;
                case 8:
                    Effects.add(new EffectInstanceLadder(buf));
                    break;
                case 9:
                    Effects.add(new EffectInstanceMount(buf));
                    break;
                case 10:
                    Effects.add(new EffectInstanceString(buf));
                    break;

            }
        }
        buf.clear();
        return Effects;
    }

    public IoBuffer SerializeEffectInstanceDice() {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(Effects.size());
        for (EffectInstance e : Effects) {
            buff.put(e.SerializationIdentifier());
            e.toBinary(buff);
        }

        buff.flip();
        return buff;
    }

    public ObjectEffect[] ObjectEffects(List<EffectInstance> effects) {
        ObjectEffect[] array = new ObjectEffect[effects.size()];
        for (int i = 0; i < array.length; ++i) {
            //EffectInstanceCreate 
            if (effects.get(i) instanceof EffectInstanceDuration) {
                array[i] = new ObjectEffectDuration((effects.get(i)).effectId, ((EffectInstanceDuration) effects.get(i)).days, ((EffectInstanceDuration) effects.get(i)).hours, ((EffectInstanceDuration) effects.get(i)).minutes);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceLadder) {
                array[i] = new ObjectEffectLadder((effects.get(i)).effectId, ((EffectInstanceLadder) effects.get(i)).monsterFamilyId, ((EffectInstanceLadder) effects.get(i)).monsterCount);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceCreature) {
                array[i] = new ObjectEffectCreature((effects.get(i)).effectId, ((EffectInstanceCreature) effects.get(i)).monsterFamilyId);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceMount) {
                array[i] = new ObjectEffectMount((effects.get(i)).effectId, ((EffectInstanceMount) effects.get(i)).date, ((EffectInstanceMount) effects.get(i)).modelId, ((EffectInstanceMount) effects.get(i)).mountId);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceString) {
                array[i] = new ObjectEffectString((effects.get(i)).effectId, ((EffectInstanceString) effects.get(i)).text);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceMinMax) {
                array[i] = new ObjectEffectMinMax((effects.get(i)).effectId, ((EffectInstanceMinMax) effects.get(i)).MinValue, ((EffectInstanceMinMax) effects.get(i)).MaxValue);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceDate) {
                array[i] = new ObjectEffectDate((effects.get(i)).effectId, ((EffectInstanceDate) effects.get(i)).Year, ((EffectInstanceDate) effects.get(i)).Mounth, ((EffectInstanceDate) effects.get(i)).Day, ((EffectInstanceDate) effects.get(i)).Hour, ((EffectInstanceDate) effects.get(i)).Minute);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceDice) {
                array[i] = new ObjectEffectDice((effects.get(i)).effectId, ((EffectInstanceDice) effects.get(i)).diceNum, ((EffectInstanceDice) effects.get(i)).diceSide, ((EffectInstanceInteger) effects.get(i)).value);
                continue;
            }
            if (effects.get(i) instanceof EffectInstanceInteger) {
                array[i] = new ObjectEffectInteger((effects.get(i)).effectId, ((EffectInstanceInteger) effects.get(i)).value);
            }

        }
        return array;
    }

    public boolean AreConditionFilled(Player character) {
        try {
            if (this.Template().CriteriaExpression() == null) {
                return true;
            } else {
                return this.Template().CriteriaExpression().Eval(character);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isWeapon() {
        return ItemDAO.Cache.get(TemplateId) instanceof Weapon;
    }

    private void ParseStats() {
        this.myStats = new GenericStats();

        StatsEnum Stat;
        for (EffectInstance e : this.Effects) {
            if (e instanceof EffectInstanceInteger) {
                Stat = StatsEnum.valueOf(e.effectId);
                if (Stat == null) {
                    Main.Logs().writeError("Undefinied Stat id " + e.effectId);
                    continue;
                }
                this.myStats.AddItem(Stat, ((EffectInstanceInteger) e).value);
            }
        }
        Stat = null;
    }

    public GenericStats GetStats() {
        if (this.myStats == null) {
            this.ParseStats();
        }
        return myStats;
    }

    public void totalClear() {
        try {
            ID = 0;
            TemplateId = 0;
            Position = 0;
            Owner = 0;
            Quantity = 0;
            for (EffectInstance e : Effects) {
                e.totalClear();
            }
            Effects.clear();
            Effects = null;
            NeedInsert = false;
            NeedRemove = false;
            ColumsToUpdate.clear();
            ColumsToUpdate = null;
            if (this.myStats == null) {
                myStats.totalClear();
                myStats = null;
            }
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    private boolean IsTokenItem() {
        return false;
    }

}
