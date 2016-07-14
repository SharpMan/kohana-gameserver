package koh.game.entities.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.GenericStats;
import koh.game.entities.item.animal.MountInventoryItem;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.data.items.ObjectEffect;
import koh.protocol.types.game.data.items.ObjectItem;
import koh.protocol.types.game.data.items.effects.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 * TODO make ObjectItem created at first sight
 * TODO2 check mimcryHandler in this case
 */
public class InventoryItem {

    private static final Logger logger = LogManager.getLogger(InventoryItem.class);

    @Getter
    private int ID;
    @Getter
    protected int templateId;
    @Getter
    private int position,quantity,owner;
    @Getter
    protected List<ObjectEffect> effects;
    @Getter @Setter
    private boolean needInsert, needRemove;
    public List<String> columsToUpdate = null;

    public InventoryItem() {

    }

    private GenericStats myStats;

    public static InventoryItem getInstance(int ID, int templateId, int position, int owner, int quantity, List<ObjectEffect> effects1) {
        if (DAO.getItemTemplates().getTemplate(templateId).getSuperType() == ItemSuperTypeEnum.SUPERTYPE_PET) {
            return new PetsInventoryItem(ID, templateId, position, owner, quantity, effects1, !effects1.stream().anyMatch(x -> x.actionId == 995));
        } else if (DAO.getItemTemplates().getTemplate(templateId).getTypeId() == 97) {
            return new MountInventoryItem(ID, templateId, position, owner, quantity, effects1, !effects1.stream().anyMatch(x -> x.actionId == 995));
        } else {
            return new InventoryItem(ID, templateId, position, owner, quantity, effects1);
        }
    }

    public InventoryItem(int ID, int templateId, int position, int owner, int quantity, List<ObjectEffect> effects) {
        this.ID = ID;
        this.templateId = templateId;
        this.position = position;
        this.owner = owner;
        this.quantity = quantity;
        this.effects = effects;
    }

    public ObjectItem getObjectItem(int withQuantity) {
        return new ObjectItem(this.position, this.templateId, effects.stream().filter(Effect -> this.getTemplate().isVisibleInTooltip(Effect.actionId)).toArray(ObjectEffect[]::new), this.ID, withQuantity);
    }

    public ObjectItem getObjectItem() {
        return new ObjectItem(this.position, this.templateId, effects.stream().filter(Effect -> this.getTemplate().isVisibleInTooltip(Effect.actionId)).toArray(ObjectEffect[]::new), this.ID, this.quantity);
    }

    public ItemSuperTypeEnum getSuperType() {
        try {
            return ItemSuperTypeEnum.valueOf(DAO.getItemTemplates().getType(getTemplate().getTypeId()).getSuperType());
        }
        catch (Exception e){
            //logger.error(this.getTemplate().toString());
            //TODO classify useless item ex id=16820,typeId=184,nameId=Sachet de Cendres ernelles
            return ItemSuperTypeEnum.SUPERTYPE_UNKNOWN_0;
        }
    }

    public boolean isEquiped() {
        return this.position != 63;
    }

    public boolean isLinked() { //928 = Lié 983 = Non echangeable
        return this.hasEffect(982) || this.hasEffect(983) || this.getSuperType() == ItemSuperTypeEnum.SUPERTYPE_QUEST || this.isTokenItem();
    }

    public boolean isLivingObject() {
        return this.getTemplate().getTypeId() == 113 || (this.getEffect(970) != null && this.getEffect(971) != null);
    }

    public short getApparrance() {
        final ObjectEffectInteger effect = (ObjectEffectInteger) this.getEffect(972);
        if (effect == null) {
            return this.getTemplate().getAppearanceId();
        } else {
            final ObjectEffectInteger type = (ObjectEffectInteger) this.getEffect(970);
            if (type == null) {
                return this.getTemplate().getAppearanceId();
            }
            return (short) ItemLivingObject.getObviAppearanceBySkinId(effect.value, type.value);
        }
    }

    public void setPosition(int i) {
        this.position = i;
        this.notifyColumn("position");
    }

    public void setQuantity(int i) {
        this.quantity = i;
        this.notifyColumn("stack");
    }

    public void setOwner(int i) {
        this.owner = i;
        this.notifyColumn("owner");
    }

    public void notifyColumn(String C) {
        if (this.columsToUpdate == null) {
            this.columsToUpdate = new ArrayList<>();
        }
        if (!this.columsToUpdate.contains(C)) {
            this.columsToUpdate.add(C);
        }
    }

    public boolean hasEffect(int id) {
        return this.effects.stream().anyMatch(x -> x.actionId == id);
    }

    public ObjectEffect getEffect(int id) {
        return this.effects.stream().filter(x -> x.actionId == id).findFirst().orElse(null);
    }

    public ItemTemplate getTemplate() {
        return DAO.getItemTemplates().getTemplate(templateId);
    }

    public ItemType getItemType() {
        return DAO.getItemTemplates().getType(getTemplate().getTypeId());
    }

    public Weapon getWeaponTemplate() {
        return (Weapon) DAO.getItemTemplates().getTemplate(templateId);
    }

    public CharacterInventoryPositionEnum getSlot() {
        return CharacterInventoryPositionEnum.valueOf((byte) this.position);
    }

    public void removeEffect(int... id) {
        for (int i : id) {
            this.removeEffect(i);
        }
    }

    private void removeEffect(int id) {
        if (this.effects.removeIf(x -> x.actionId == id)) {
            this.notifyColumn("effects");
        }
    }

    public List<ObjectEffect> getEffects$Notify() {
        this.notifyColumn("effects");
        return effects;
    }

    public List<ObjectEffect> getEffectsCopy() {
        List<ObjectEffect> effects = new ArrayList<>();
        this.effects.stream().forEach((e) -> { //TODO: Parralel Stream
            effects.add(e.Clone());
        });
        return effects;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public int getWeight() {
        return this.getTemplate().getRealWeight() * this.quantity;
    }

    public void setPosition(CharacterInventoryPositionEnum Slot) {
        this.position = Slot.value();
        this.notifyColumn("position");
    }

    public boolean Equals(Collection<ObjectEffect> Effects) {
        ObjectEffect Get = null;
        if (Effects.size() != this.effects.size()) {
            return false;
        }
        for (ObjectEffect e : Effects) {
            Get = this.getEffect(e.actionId);
            if (Get == null || !Get.equals(e)) {
                return false;
            }
        }
        return true;
    }

    public static List<ObjectEffect> deserializeEffects(byte[] binary) {
        IoBuffer buf = IoBuffer.wrap(binary);
        int len = buf.getInt();
        List<ObjectEffect> Effects = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            Effects.add(ObjectEffect.Instance(buf.getInt()));
            Effects.get(i).deserialize(buf);
        }
        buf.clear();
        return Effects;
    }

    public IoBuffer serializeEffectInstanceDice() {
        IoBuffer buff = IoBuffer.allocate(2535);
        buff.setAutoExpand(true);

        buff.putInt(effects.size());
        for (ObjectEffect e : effects) {
            buff.putInt(e.getTypeId());
            e.serialize(buff);
        }

        buff.flip();
        return buff;
    }

    public boolean areConditionFilled(Player character) {
        try {
            if (this.getTemplate().getCriteriaExpression() == null) {
                return true;
            } else {
                return this.getTemplate().getCriteriaExpression().eval(character);
            }
        } catch (Exception e) {
            logger.error("Bugged item {} condition {}", this.getTemplate().getId(), this.getTemplate().getCriteria());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isWeapon() {
        return DAO.getItemTemplates().getTemplate(templateId) instanceof Weapon;
    }

    private void parseStats() {
        this.myStats = new GenericStats();

        StatsEnum Stat;
        for (ObjectEffect e : this.effects) {
            if (e instanceof ObjectEffectInteger) {
                Stat = StatsEnum.valueOf(e.actionId);
                if (Stat == null) {
                    logger.error("Undefined Stat id {}", e.actionId);
                    continue;
                }
                this.myStats.addItem(Stat, ((ObjectEffectInteger) e).value);
            }else if(e.actionId == EffectHelper.SPELL_EFFECT_PER_FIGHT){
                this.myStats.addItem(StatsEnum.CAST_SPELL_ON_CRITICAL_HIT, ((ObjectEffectDice) e).diceNum);
            }
        }
        Stat = null;
    }

    public GenericStats getStats() {
        if (this.myStats == null) {
            this.parseStats();
            if (this.getTemplate() instanceof Weapon) {
                this.getWeaponTemplate().initialize();
            }
        }
        return myStats;
    }

    public void totalClear() {
        try {
            ID = 0;
            templateId = 0;
            position = 0;
            owner = 0;
            quantity = 0;
            //TODO: redo this algo
            /*for (ObjectEffect e : effects) {
             e.totalClear();
             }*/
            effects.clear();
            effects = null;
            needInsert = false;
            needRemove = false;
            columsToUpdate.clear();
            columsToUpdate = null;
            if (this.myStats == null) {
                myStats.totalClear();
                myStats = null;
            }
            this.finalize();
        } catch (Throwable tr) {
        }
    }

    private boolean isTokenItem() {
        return this.templateId == 13470 || this.templateId == 12736;
    }

}
