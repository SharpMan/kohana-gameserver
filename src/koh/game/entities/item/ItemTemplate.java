package koh.game.entities.item;

import com.google.common.base.Strings;
import java.util.Arrays;
import koh.game.Main;
import koh.game.conditions.ConditionExpression;
import koh.game.dao.ItemDAO;
import koh.game.entities.spells.EffectInstance;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class ItemTemplate {
    
    public int id;
    public String nameId;
    public int TypeId;
    public int iconId, level, realWeight;
    public boolean cursed;
    public int useAnimationId;
    public boolean usable, targetable, exchangeable;
    public float price;
    public boolean twoHanded, etheral;
    public int itemSetId;
    public String criteria, criteriaTarget;
    public boolean hideEffects, enhanceable, nonUsableOnAnother;
    public short appearanceId;
    public boolean secretRecipe;
    public int recipeSlots;
    public int[] recipeIds, dropMonsterIds;
    public boolean bonusIsSecret;
    public EffectInstance[] possibleEffects;
    public int[] favoriteSubAreas;
    public int favoriteSubAreasBonus;
    private ConditionExpression m_criteriaExpression;
    
    public ConditionExpression CriteriaExpression() {
        try {
            if (m_criteriaExpression == null) {
                if (Strings.isNullOrEmpty(criteria) || this.criteria.equalsIgnoreCase("null")) {
                    return null;
                } else {
                    this.m_criteriaExpression = ConditionExpression.Parse(this.criteria);
                }
            }
            return m_criteriaExpression;
        } catch (Error e) {
            Main.Logs().writeError(this.toString());
            e.printStackTrace();
            return null;
        }
    }
    
    public EffectInstance GetEffect(int uid) {
        return Arrays.stream(possibleEffects).filter(x -> x.effectId == uid).findFirst().orElse(null);
    }
    
    public boolean isVisibleInTooltip(int Effect) {
        return !Arrays.stream(this.possibleEffects).anyMatch(x -> x.effectId == Effect && !x.visibleInTooltip);
    }
    
    public ItemSet ItemSet() {
        return this.itemSetId < 0 ? null : ItemDAO.Sets.get(this.itemSetId);
    }
    
    public ItemSuperTypeEnum GetSuperType() {
        try {
            return ItemSuperTypeEnum.valueOf(ItemDAO.SuperTypes.get(TypeId).SuperType);
        } catch (java.lang.NullPointerException e) {
            return ItemSuperTypeEnum.SUPERTYPE_UNKNOWN_0;
        }
    }
    
    public static boolean CanPlaceInSlot(ItemSuperTypeEnum Type, CharacterInventoryPositionEnum Slot) {
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
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
