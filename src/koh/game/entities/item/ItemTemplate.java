package koh.game.entities.item;

import com.google.common.base.Strings;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import koh.game.conditions.ConditionExpression;
import koh.game.dao.DAO;
import koh.game.dao.api.ItemTemplateDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.item.actions.LearnSpell;
import koh.game.entities.spells.EffectInstance;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ItemTemplate {

    @Getter
    protected int id,typeId;
    @Getter
    private String nameId;
    @Getter
    private int iconId, level, realWeight,useAnimationId,itemSetId;
    @Getter
    private boolean cursed;
    @Getter
    private boolean usable, targetable, exchangeable;
    @Getter
    private float price;
    @Getter
    private boolean twoHanded, etheral;
    @Getter
    private String criteria, criteriaTarget;
    @Getter
    private boolean hideEffects, enhanceable, nonUsableOnAnother;
    @Getter
    private short appearanceId;
    @Getter
    private boolean secretRecipe,bonusIsSecret;
    @Getter
    private int recipeSlots;
    @Getter
    private int[] recipeIds, dropMonsterIds;
    @Getter
    protected EffectInstance[] possibleEffects;
    @Getter
    private int[] favoriteSubAreas;
    @Getter
    private int favoriteSubAreasBonus;
    @Getter
    private ConditionExpression m_criteriaExpression;
    private ArrayList<ItemAction> actions = null;

    private static final Logger logger = LogManager.getLogger(ItemTemplate.class);

    public ItemTemplate(ResultSet result) throws SQLException {
        id = result.getInt("id");
        this.nameId = result.getString("name");
        this.typeId = result.getInt("type_id");
        this.iconId = result.getInt("icon_id");
        this.level = result.getInt("level");
        this.realWeight = result.getInt("real_weight");
        this.cursed = result.getBoolean("cursed");
        this.useAnimationId = result.getInt("use_animation_id");
        this.usable = result.getBoolean("usable");
        this.targetable = result.getBoolean("targetable");
        this.exchangeable = result.getBoolean("exchangeable");
        this.price = result.getFloat("price");
        this.twoHanded = result.getBoolean("two_handed");
        this.etheral = result.getBoolean("etheral");
        this.itemSetId = result.getInt("item_set_id");
        this.criteria = result.getString("criteria");
        this.criteriaTarget = result.getString("criteria_target");
        this.hideEffects = result.getBoolean("hide_effects");
        this.enhanceable = result.getBoolean("enhanceable");
        this.nonUsableOnAnother = result.getBoolean("non_usable_on_another");
        this.appearanceId = result.getShort("appearance_id");
        this.secretRecipe = result.getBoolean("secret_recipe");
        this.recipeSlots = result.getInt("recipe_slots");
        this.recipeIds = ItemTemplateDAO.parseIds(result.getString("recipe_ids"));
        this.dropMonsterIds = ItemTemplateDAO.parseIds(result.getString("drop_monster_ids"));
        this.bonusIsSecret = result.getBoolean("bonus_is_secret");
        this.possibleEffects = ItemTemplateDAO.readDiceEffects(result.getBytes("possible_effects"));
        this.favoriteSubAreas = ItemTemplateDAO.parseIds(result.getString("favorite_sub_areas"));
        this.favoriteSubAreasBonus = result.getInt("favorite_sub_areas_bonus");
    }

    public ConditionExpression getCriteriaExpression() {
        try {
            if (m_criteriaExpression == null) {
                if (Strings.isNullOrEmpty(criteria) || this.criteria.equalsIgnoreCase("null")) {
                    return null;
                } else {
                    this.m_criteriaExpression = ConditionExpression.parse(this.criteria.trim());
                }
            }
            return m_criteriaExpression;
        } catch (Error e) {
            logger.error(this.toString());
            e.printStackTrace();
            return null;
        }
    }
    
    public EffectInstance getEffect(int uid) {
        return Arrays.stream(possibleEffects).filter(x -> x.effectId == uid).findFirst().orElse(null);
    }
    
    public boolean isVisibleInTooltip(int Effect) {
        return !Arrays.stream(this.possibleEffects).anyMatch(x -> x.effectId == Effect && !x.visibleInTooltip);
    }
    
    public ItemSet getItemSet() {
        return this.itemSetId < 0 ? null : DAO.getItemTemplates().getSet(this.itemSetId);
    }
    
    public ItemSuperTypeEnum getSuperType() {
        try {
            return ItemSuperTypeEnum.valueOf(DAO.getItemTemplates().getType(typeId).getSuperType());
        } catch (java.lang.NullPointerException e) {
            return ItemSuperTypeEnum.SUPERTYPE_UNKNOWN_0;
        }
    }
    
    public static boolean canPlaceInSlot(ItemSuperTypeEnum Type, CharacterInventoryPositionEnum Slot) {
        //TODO Living Object
        switch (Type) {
            case SUPERTYPE_AMULET:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_AMULET) {
                    return true;
                }
                break;
            
            case SUPERTYPE_WEAPON:
            case SUPERTYPE_WEAPON_7:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_WEAPON) {
                    return true;
                }
                break;
            
            case SUPERTYPE_RING:
                if (Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_RING_LEFT || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_RING_RIGHT) {
                    return true;
                }
                break;
            
            case SUPERTYPE_CAPE:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_CAPE) {
                    return true;
                }
                break;
            
            case SUPERTYPE_HAT:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_HAT) {
                    return true;
                }
                break;
            
            case SUPERTYPE_BOOTS:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_BOOTS) {
                    return true;
                }
                break;
            case SUPERTYPE_BELT:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_BELT) {
                    return true;
                }
                break;
            
            case SUPERTYPE_PET:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_PETS) {
                    return true;
                }
                break;
            
            case SUPERTYPE_SHIELD:
                if (Slot == CharacterInventoryPositionEnum.ACCESSORY_POSITION_SHIELD) {
                    return true;
                }
                break;
            
            case SUPERTYPE_DOFUS:
                if (Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_1
                        || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_2
                        || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_3
                        || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_4
                        || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_5
                        || Slot == CharacterInventoryPositionEnum.INVENTORY_POSITION_DOFUS_6) {
                    return true;
                }
                break;
        }
        return false;
    }

    public void addItemAction(ItemAction action){
        if(this.actions == null)
            this.actions = new ArrayList<>(2);
        this.actions.add(action);
    }

    public boolean hasAction(Class<? extends ItemAction> klass){
        return actions != null && actions.stream().anyMatch(ac -> ac.getClass().isAssignableFrom(klass));
    }


    public boolean use(Player plr,int cell){
        if(this.actions == null) {
            return false;
        }
        return this.actions.stream().allMatch(x -> x.execute(plr,cell));
    }


    public boolean isWeapon(){
        return this instanceof  Weapon;
    }


    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
