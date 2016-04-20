package koh.game.entities.actors.character;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.BreedEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;
import lombok.Getter;
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

    @Getter
    private Map<StatsEnum, CharacterBaseCharacteristic> stats = Collections.synchronizedMap(new HashMap<>());

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
            put(StatsEnum.SUB_RANGE_LAUNCHER, StatsEnum.ADD_RANGE_LAUNCHER);
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
            put(StatsEnum.SUB_DAMAGE_BONUS_PERCENT, StatsEnum.ADD_DAMAGE_PERCENT);
        }
    };

    public GenericStats() {

    }

    public GenericStats(Player character) {
        this.stats.put(StatsEnum.ACTION_POINTS, new CharacterBaseCharacteristic(character.getLevel() >= 100 ? 7 : 6));
        this.stats.put(StatsEnum.MOVEMENT_POINTS, new CharacterBaseCharacteristic(3));
        this.stats.put(StatsEnum.PROSPECTING, new CharacterBaseCharacteristic((character.getBreed() == BreedEnum.Enutrof ? 120 : 100)));
        this.stats.put(StatsEnum.ADD_PODS, new CharacterBaseCharacteristic(1000));
        this.stats.put(StatsEnum.ADD_SUMMON_LIMIT, new CharacterBaseCharacteristic(1));
        this.stats.put(StatsEnum.INITIATIVE, new CharacterBaseCharacteristic(100));

        this.stats.put(StatsEnum.VITALITY, new CharacterBaseCharacteristic(character.getVitality()));
        this.stats.put(StatsEnum.WISDOM, new CharacterBaseCharacteristic(character.getWisdom()));
        this.stats.put(StatsEnum.STRENGTH, new CharacterBaseCharacteristic(character.getStrength()));
        this.stats.put(StatsEnum.INTELLIGENCE, new CharacterBaseCharacteristic(character.getIntell()));
        this.stats.put(StatsEnum.AGILITY, new CharacterBaseCharacteristic(character.getAgility()));
        this.stats.put(StatsEnum.CHANCE, new CharacterBaseCharacteristic(character.getChance()));
    }

    public Map<StatsEnum, CharacterBaseCharacteristic> getEffects() {
        return this.stats;
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
        if (!this.stats.containsKey(key)) {
            this.stats.get(key).unMerge(stat);
        }
    }

    public void merge(GenericStats stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> effect : stats.getEffects().entrySet()) {
            if(OPPOSITE_STATS.containsKey(effect.getKey())){
                this.stats.get(OPPOSITE_STATS.get(effect.getKey())).unMerge(effect.getValue());
                continue;
            }
            if (!this.stats.containsKey(effect.getKey())) {
                this.stats.put(effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.stats.get(effect.getKey()).merge(effect.getValue());
        }
    }

    public int totalBasePoints(){
        return this.stats.values().stream()
                .filter(stat -> stat.base > 0)
                .mapToInt(stat -> stat.base)
                .sum();
    }

    public void unMerge(GenericStats Stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> effect : Stats.getEffects().entrySet()) {
            if(OPPOSITE_STATS.containsKey(effect.getKey())){
                this.stats.get(OPPOSITE_STATS.get(effect.getKey())).merge(effect.getValue());
                continue;
            }
            if (!this.stats.containsKey(effect.getKey())) {
                this.stats.put(effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.stats.get(effect.getKey()).unMerge(effect.getValue());
        }
    }

    public void reset() {
        this.stats.values().forEach(x -> {
            x.base = 0;
            x.additionnal = 0;
            x.alignGiftBonus = 0;
            x.contextModif = 0;
            x.objectsAndMountBonus = 0;
        });
    }


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
        if (stats.containsKey(effectType)) {
            total += stats.get(effectType).getTotal();
        }

        switch (effectType) {
            /*case getInitiative:
             getTotal += getTotal(StatsEnum.strength) + getTotal(StatsEnum.chance) + getTotal(StatsEnum.intelligence) + getTotal(StatsEnum.agility);*/
            case DODGE_PA_LOST_PROBABILITY:
            case DODGE_PM_LOST_PROBABILITY:
            case ADD_RETRAIT_PA:
            case ADD_RETRAIT_PM:
                total += getTotal(StatsEnum.WISDOM) / 4;
                break;
            case ACTION_POINTS:
                if (isCharacterFighter && (total - stats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pa")) {
                    total -= (total - stats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pa");
                }
                total += getTotal(StatsEnum.ADD_PA_BIS);
                break;
            case MOVEMENT_POINTS:
                if (isCharacterFighter && (total - stats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pm")) {
                    total -= (total - stats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pm");
                }
                total += getTotal(StatsEnum.ADD_PM);
                break;
            case ADD_TACKLE_BLOCK:
            case ADD_TACKLE_EVADE:
                total += getTotal(StatsEnum.AGILITY) / 10;
            /*case Reflect:
             getTotal += getTotal(StatsEnum.AddRenvoiDamageItem);
             break;*/
        }

        return total;
    }

    public CharacterBaseCharacteristic getEffect(StatsEnum effectType) {
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.stats.get(effectType);
    }

    public CharacterBaseCharacteristic getEffect(int effectType) {
        return getEffect(StatsEnum.valueOf(effectType));
    }

    public void addBase(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addBase(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic(value, 0, 0, 0, 0));
        } else {
            this.stats.get(effectType).base += value;
        }
    }

    public void resetBase() {
        this.stats.values().forEach(stat -> stat.base = 0);
    }

    public void addBoost(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addBoost(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic(0, value, 0, 0, 0));
        } else {
            this.stats.get(effectType).additionnal += value;
        }
    }

    public int getBase(StatsEnum effectType) {
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic());
        }
        switch (effectType) {
            /*case getInitiative:
             return this.stats.get(effectType).base + getTotal(StatsEnum.strength) + getTotal(StatsEnum.chance) + getTotal(StatsEnum.intelligence) + getTotal(StatsEnum.agility);*/
            default:
                return this.stats.get(effectType).base;
        }
    }

    public int getBoost(StatsEnum effectType) {
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.stats.get(effectType).additionnal;
    }

    public int getItem(StatsEnum effectType) {
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic());
        }
        return this.stats.get(effectType).objectsAndMountBonus;
    }

    public void addItem(StatsEnum effectType, int value) {
        if (OPPOSITE_STATS.containsKey(effectType)) {
            this.addItem(OPPOSITE_STATS.get(effectType), -value);
            return;
        }
        if (!this.stats.containsKey(effectType)) {
            this.stats.put(effectType, new CharacterBaseCharacteristic(0, 0, value, 0, 0));
        } else {
            this.stats.get(effectType).objectsAndMountBonus += value;
        }
    }

    public void totalClear() {
        try {
            this.stats.values().forEach(CharacterBaseCharacteristic::totalClear);
            stats.clear();
            stats = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
