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
    public List<ObjectEffect> Effects; //FIXME : Think if we should migrate to Array or not , trought newArray = ArraysUtils.add(T[] Array,T Element);
    public boolean NeedInsert, NeedRemove;
    public List<String> ColumsToUpdate = null;

    public InventoryItem() {

    }

    private GenericStats myStats;

    public static InventoryItem Instance(int ID, int TemplateId, int Position, int Owner, int Quantity, List<ObjectEffect> Effects) {
        if (ItemDAO.Cache.get(TemplateId).GetSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
            if (Effects.stream().anyMatch(x -> x.actionId == 995)) {
                Main.Logs().writeDebug("Contains Aninal");
                return new PetsInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, false);
            } else {
                Main.Logs().writeDebug("Create Animal");
                return new PetsInventoryItem(ID, TemplateId, Position, Owner, Quantity, Effects, true);
            }
        } else if (ItemDAO.Cache.get(TemplateId).TypeId == 97) {
            if (Effects.stream().anyMatch(x -> x.actionId == 995)) {
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

    public InventoryItem(int ID, int TemplateId, int Position, int Owner, int Quantity, List<ObjectEffect> Effects) {
        this.ID = ID;
        this.TemplateId = TemplateId;
        this.Position = Position;
        this.Owner = Owner;
        this.Quantity = Quantity;
        this.Effects = Effects;
    }

    public ObjectItem ObjectItem(int WithQuantity) {
        return new ObjectItem(this.Position, this.TemplateId, Effects.stream().filter(Effect -> this.Template().isVisibleInTooltip(Effect.actionId)).toArray(ObjectEffect[]::new), this.ID, WithQuantity);
    }

    public ObjectItem ObjectItem() {
        return new ObjectItem(this.Position, this.TemplateId, Effects.stream().filter(Effect -> this.Template().isVisibleInTooltip(Effect.actionId)).toArray(ObjectEffect[]::new), this.ID, this.Quantity);
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
        ObjectEffectInteger effect = (ObjectEffectInteger) this.GetEffect(972);
        if (effect == null) {
            return this.Template().appearanceId;
        } else {
            ObjectEffectInteger type = (ObjectEffectInteger) this.GetEffect(970);
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
        return this.Effects.stream().anyMatch(x -> x.actionId == id);
    }

    public ObjectEffect GetEffect(int id) {
        return this.Effects.stream().filter(x -> x.actionId == id).findFirst().orElse(null);
    }

    public ItemTemplate Template() {
        return ItemDAO.Cache.get(TemplateId);
    }

    public CharacterInventoryPositionEnum Slot() {
        return CharacterInventoryPositionEnum.valueOf((byte) this.Position);
    }

    public void RemoveEffect(int id) {
        if (this.Effects.removeIf(x -> x.actionId == id)) {
            this.NotifiedColumn("effects");
        }
    }

    public List<ObjectEffect> getEffects() {
        this.NotifiedColumn("effects");
        return Effects;
    }

    public List<ObjectEffect> getEffectsCopy() {
        List<ObjectEffect> effects = new ArrayList<>();
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

    public boolean Equals(Collection<ObjectEffect> Effects) {
        ObjectEffect Get = null;
        if (Effects.size() != this.Effects.size()) {
            return false;
        }
        for (ObjectEffect e : Effects) {
            Get = this.GetEffect(e.actionId);
            if (Get == null || !Get.equals(e)) {
                return false;
            }
        }
        return true;
    }

    public static List<ObjectEffect> DeserializeEffects(byte[] binary) {
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        List<ObjectEffect> Effects = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            Effects.add(ObjectEffect.Instance(buf.getInt()));
            Effects.get(i).deserialize(buf);
        }
        buf.clear();
        return Effects;
    }

    public IoBuffer SerializeEffectInstanceDice() {
        IoBuffer buff = IoBuffer.allocate(65535);
        buff.setAutoExpand(true);

        buff.putInt(Effects.size());
        for (ObjectEffect e : Effects) {
            buff.putInt(e.getTypeId());
            e.serialize(buff);
        }

        buff.flip();
        return buff;
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
        for (ObjectEffect e : this.Effects) {
            if (e instanceof ObjectEffectInteger) {
                Stat = StatsEnum.valueOf(e.actionId);
                if (Stat == null) {
                    Main.Logs().writeError("Undefinied Stat id " + e.actionId);
                    continue;
                }
                this.myStats.AddItem(Stat, ((ObjectEffectInteger) e).value);
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
            /*for (ObjectEffect e : Effects) {
             e.totalClear();
             }*/
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
