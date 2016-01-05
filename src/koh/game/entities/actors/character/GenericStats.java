package koh.game.entities.actors.character;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.BreedEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import lombok.ToString;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Neo-Craft
 */
@ToString
public class GenericStats {

    private Map<StatsEnum, CharacterBaseCharacteristic> myStats = Collections.synchronizedMap(new HashMap<>());

    private static final Map<StatsEnum, StatsEnum> OPPOSITE_STATS = new HashMap<StatsEnum, StatsEnum>() {
        {
            put(StatsEnum.SUB_NEUTRAL_ELEMENT_REDUCTION, StatsEnum.NEUTRAL_ELEMENT_REDUCTION);
            put(StatsEnum.SUB_CRITICAL_HIT, StatsEnum.ADD_CRITICAL_HIT);
            put(StatsEnum.SUB_INTELLIGENCE, StatsEnum.INTELLIGENCE);
            put(StatsEnum.SUB_PM, StatsEnum.MOVEMENT_POINTS);
            put(StatsEnum.SUB_PVP_WATER_RESIST_PERCENT, StatsEnum.PVP_WATER_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_TACKLE_EVADE, StatsEnum.ADD_TACKLE_EVADE);
            put(StatsEnum.SUB_WATER_RESIST_PERCENT, StatsEnum.WATER_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_VITALITY, StatsEnum.VITALITY);
            put(StatsEnum.SUB_NEUTRAL_RESIST_PERCENT, StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_RETRAIT_PM, StatsEnum.ADD_RETRAIT_PM);
            put(StatsEnum.SUB_AIR_RESIST_PERCENT, StatsEnum.AIR_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_CRITICAL_DAMAGES_REDUCTION, StatsEnum.ADD_CRITICAL_DAMAGES_REDUCTION);
            put(StatsEnum.SUB_PVP_AIR_RESIST_PERCENT, StatsEnum.PVP_AIR_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_RETRAIT_PA, StatsEnum.ADD_RETRAIT_PA);
            put(StatsEnum.SUB_TACKLE_BLOCK, StatsEnum.ADD_TACKLE_BLOCK);
            put(StatsEnum.SUB_FIRE_DAMAGES_BONUS, StatsEnum.ADD_FIRE_DAMAGES_BONUS);
            put(StatsEnum.SUB_CRITICAL_DAMAGES, StatsEnum.ADD_CRITICAL_DAMAGES);
            put(StatsEnum.SUB_NEUTRAL_DAMAGES_BONUS, StatsEnum.ADD_NEUTRAL_DAMAGES_BONUS);
            put(StatsEnum.SUB_DODGE_PM_PROBABILITY, StatsEnum.DODGE_PM_LOST_PROBABILITY);
            put(StatsEnum.SUB_EARTH_RESIST_PERCENT, StatsEnum.EARTH_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_WISDOM, StatsEnum.WISDOM);
            put(StatsEnum.SUB_INITIATIVE, StatsEnum.INITIATIVE);
            put(StatsEnum.SUB_WATER_DAMAGES_BONUS, StatsEnum.ADD_WATER_DAMAGES_BONUS);
            put(StatsEnum.SUB_AGILITY, StatsEnum.AGILITY);
            put(StatsEnum.SUB_EARTH_ELEMENT_REDUCTION, StatsEnum.EARTH_ELEMENT_REDUCTION);
            put(StatsEnum.SUB_PVP_FIRE_RESIST_PERCENT, StatsEnum.PVP_FIRE_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_PVP_EARTH_RESIST_PERCENT, StatsEnum.PVP_EARTH_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_FIRE_RESIST_PERCENT, StatsEnum.FIRE_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_DODGE_PA_PROBABILITY, StatsEnum.DODGE_PA_LOST_PROBABILITY);
            put(StatsEnum.SUB_WATER_ELEMENT_REDUCTION, StatsEnum.WATER_ELEMENT_REDUCTION);
            put(StatsEnum.SUB_PUSH_DAMAGES_BONUS, StatsEnum.ADD_PUSH_DAMAGES_BONUS);
            put(StatsEnum.SUB_RANGE, StatsEnum.ADD_RANGE);
            put(StatsEnum.SUB_DAMAGE, StatsEnum.ADD_DAMAGE_MAGIC);
            put(StatsEnum.SUB_AIR_ELEMENT_REDUCTION, StatsEnum.AIR_ELEMENT_REDUCTION);
            put(StatsEnum.SUB_AIR_DAMAGES_BONUS, StatsEnum.ADD_AIR_DAMAGES_BONUS);
            put(StatsEnum.SUB_FIRE_ELEMENT_REDUCTION, StatsEnum.FIRE_ELEMENT_REDUCTION);
            put(StatsEnum.SUB_PROSPECTING, StatsEnum.PROSPECTING);
            put(StatsEnum.SUB_EARTH_DAMAGES_BONUS, StatsEnum.ADD_EARTH_DAMAGES_BONUS);
            put(StatsEnum.SUB_PA, StatsEnum.ACTION_POINTS);
            put(StatsEnum.SUB_STRENGTH, StatsEnum.STRENGTH);
            put(StatsEnum.SUB_PUSH_DAMAGES_REDUCTION, StatsEnum.ADD_PUSH_DAMAGES_REDUCTION);
            put(StatsEnum.SUB_PVP_NEUTRAL_RESIST_PERCENT, StatsEnum.PVP_NEUTRAL_ELEMENT_RESIST_PERCENT);
            put(StatsEnum.SUB_HEAL_BONUS, StatsEnum.ADD_HEAL_BONUS);
            put(StatsEnum.SUB_CHANCE, StatsEnum.CHANCE);
            put(StatsEnum.SUB_DAMAGE_BONUS_PERCENT, StatsEnum.ADD_DAMAGE_FINAL_PERCENT);
        }
    };

    public GenericStats() {

    }

    public GenericStats(Player character) {
        this.myStats.put(StatsEnum.ACTION_POINTS, new CharacterBaseCharacteristic(character.getLevel() >= 100 ? 7 : 6));
        this.myStats.put(StatsEnum.MOVEMENT_POINTS, new CharacterBaseCharacteristic(3));
        this.myStats.put(StatsEnum.PROSPECTING, new CharacterBaseCharacteristic((character.getBreed() == BreedEnum.Enutrof ? 120 : 100)));
        this.myStats.put(StatsEnum.AddPods, new CharacterBaseCharacteristic(1000));
        this.myStats.put(StatsEnum.ADD_SUMMON_LIMIT, new CharacterBaseCharacteristic(1));
        this.myStats.put(StatsEnum.INITIATIVE, new CharacterBaseCharacteristic(100));

        this.myStats.put(StatsEnum.VITALITY, new CharacterBaseCharacteristic(character.getVitality()));
        this.myStats.put(StatsEnum.WISDOM, new CharacterBaseCharacteristic(character.getWisdom()));
        this.myStats.put(StatsEnum.STRENGTH, new CharacterBaseCharacteristic(character.getStrength()));
        this.myStats.put(StatsEnum.INTELLIGENCE, new CharacterBaseCharacteristic(character.getIntell()));
        this.myStats.put(StatsEnum.AGILITY, new CharacterBaseCharacteristic(character.getAgility()));
        this.myStats.put(StatsEnum.CHANCE, new CharacterBaseCharacteristic(character.getChance()));
    }

    public Map<StatsEnum, CharacterBaseCharacteristic> getEffects() {
        return this.myStats;
    }

    /* TO DELETE after read id , ; explanation about this code part
        if(OPPOSITE_STATS.containsKey(effectType)){
            this.addBoost(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        I use CharacterBaseCharacteristic type class her so I don't have to create a useless class and duplicate
        at each AS code the type class. and for the OPPOSITE_STATS when i should send to the client the packet
        I should at this time cancel all opp stats that will lead me to duplicate and give a fakeValue
        for this i will not create opposite stats class and directly just cancel pour the parentOpp class

     */


    public void unMerge(StatsEnum key, CharacterBaseCharacteristic stat) {
        if (!this.myStats.containsKey(key)) {
            this.myStats.get(key).unMerge(stat);
        }
    }

    public void merge(GenericStats stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> effect : stats.getEffects().entrySet()) {
            if(OPPOSITE_STATS.containsKey(effect.getKey())){
                this.myStats.get(OPPOSITE_STATS.get(effect.getKey())).unMerge(effect.getValue());
                continue;
            }
            if (!this.myStats.containsKey(effect.getKey())) {
                this.myStats.put(effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.myStats.get(effect.getKey()).merge(effect.getValue());
        }
    }

    public void unMerge(GenericStats Stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> effect : Stats.getEffects().entrySet()) {
            if(OPPOSITE_STATS.containsKey(effect.getKey())){
                this.myStats.get(OPPOSITE_STATS.get(effect.getKey())).merge(effect.getValue());
                continue;
            }
            if (!this.myStats.containsKey(effect.getKey())) {
                this.myStats.put(effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.myStats.get(effect.getKey()).unMerge(effect.getValue());
        }
    }

    public void reset() {
        this.myStats.values().forEach(x -> {
            x.base = 0;
            x.additionnal = 0;
            x.alignGiftBonus = 0;
            x.contextModif = 0;
            x.objectsAndMountBonus = 0;
        });
    }

    //TODO
    public GenericStats clone() {
        GenericStats stats = new GenericStats();
        return stats;
    }

    public int getTotal(StatsEnum effectType) {
        return getTotal(effectType, false);
    }

    public int getTotal(StatsEnum effectType, boolean isCharacterFighter) {
        int total = 0;

        // existant ?
        if (myStats.containsKey(effectType)) {
            total += myStats.get(effectType).Total();
        }

        switch (effectType) {
            /*case getInitiative:
             Total += getTotal(StatsEnum.strength) + getTotal(StatsEnum.chance) + getTotal(StatsEnum.intelligence) + getTotal(StatsEnum.agility);*/
            case DODGE_PA_LOST_PROBABILITY:
            case DODGE_PM_LOST_PROBABILITY:
            case ADD_RETRAIT_PA:
            case ADD_RETRAIT_PM:
                total += getTotal(StatsEnum.WISDOM) / 4;
                break;
            case ACTION_POINTS:
                if (isCharacterFighter && (total - myStats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pa")) {
                    total -= (total - myStats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pa");
                }
                total += getTotal(StatsEnum.ADD_PA_BIS);
                break;
            case MOVEMENT_POINTS:
                if (isCharacterFighter && (total - myStats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pm")) {
                    total -= (total - myStats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pm");
                }
                total += getTotal(StatsEnum.ADD_PM);
                break;
            /*case Reflect:
             Total += getTotal(StatsEnum.AddRenvoiDamageItem);
             break;*/
        }

        return total;
    }

    public CharacterBaseCharacteristic getEffect(StatsEnum effectType) {
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(effectType);
    }

    public CharacterBaseCharacteristic getEffect(int effectType) {
        return getEffect(StatsEnum.valueOf(effectType));
    }

    public void addBase(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addBase(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic(value, 0, 0, 0, 0));
        } else {
            this.myStats.get(effectType).base += value;
        }
    }

    public void resetBase() {
        this.myStats.values().forEach(stat -> stat.base = 0);
    }

    public void addBoost(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addBoost(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic(0, value, 0, 0, 0));
        } else {
            this.myStats.get(effectType).additionnal += value;
        }
    }

    public int getBase(StatsEnum effectType) {
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic());
        }
        switch (effectType) {
            /*case getInitiative:
             return this.myStats.get(effectType).base + getTotal(StatsEnum.strength) + getTotal(StatsEnum.chance) + getTotal(StatsEnum.intelligence) + getTotal(StatsEnum.agility);*/
            default:
                return this.myStats.get(effectType).base;
        }
    }

    public int getBoost(StatsEnum effectType) {
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(effectType).additionnal;
    }

    public int getItem(StatsEnum effectType) {
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(effectType).objectsAndMountBonus;
    }

    public void addItem(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addItem(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic(0, 0, value, 0, 0));
        } else {
            this.myStats.get(effectType).objectsAndMountBonus += value;
        }
    }

    public void totalClear() {
        try {
            this.myStats.values().forEach(CharacterBaseCharacteristic::totalClear);
            myStats.clear();
            myStats = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
