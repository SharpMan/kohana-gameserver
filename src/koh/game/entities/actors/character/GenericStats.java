package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.BreedEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.character.characteristic.CharacterBaseCharacteristic;

/**
 *
 * @author Neo-Craft
 */
public class GenericStats {

    private Map<StatsEnum, CharacterBaseCharacteristic> myStats = Collections.synchronizedMap(new HashMap<>());

    private static Map<StatsEnum, StatsEnum> OPPOSITE_STATS = new HashMap<StatsEnum, StatsEnum>() {
        {
            put(StatsEnum.PvpEarthElementResistPercent, StatsEnum.Sub_Pvp_Earth_Resist_Percent);
            put(StatsEnum.PvpWaterElementResistPercent, StatsEnum.Sub_Pvp_Water_Resist_Percent);
            put(StatsEnum.PvpAirElementResistPercent, StatsEnum.Sub_Pvp_Air_Resist_Percent);
            put(StatsEnum.PvpFireElementResistPercent, StatsEnum.Sub_Pvp_Fire_Resist_Percent);
            put(StatsEnum.PvpNeutralElementResistPercent, StatsEnum.Sub_Pvp_Neutral_Resist_Percent);

            put(StatsEnum.NeutralElementReduction, StatsEnum.Sub_Neutral_Element_Reduction);
            put(StatsEnum.EarthElementReduction, StatsEnum.Sub_Earth_Element_Reduction);
            put(StatsEnum.WaterElementReduction, StatsEnum.Sub_Water_Element_Reduction);
            put(StatsEnum.AirElementReduction, StatsEnum.Sub_Air_Element_Reduction);
            put(StatsEnum.FireElementReduction, StatsEnum.Sub_Fire_Element_Reduction);

            put(StatsEnum.WaterElementResistPercent, StatsEnum.Sub_Water_Resist_Percent);
            put(StatsEnum.EarthElementResistPercent, StatsEnum.Sub_Earth_Resist_Percent);
            put(StatsEnum.AirElementResistPercent, StatsEnum.Sub_Air_Resist_Percent);
            put(StatsEnum.FireElementResistPercent, StatsEnum.Sub_Fire_Resist_Percent);
            put(StatsEnum.NeutralElementResistPercent, StatsEnum.Sub_Neutral_Resist_Percent);

            put(StatsEnum.Add_RETRAIT_PA, StatsEnum.Sub_RETRAIT_PA);
            put(StatsEnum.Add_RETRAIT_PM, StatsEnum.SUB_RETRAIT_PM);
            put(StatsEnum.Add_Push_Damages_Bonus, StatsEnum.Sub_Push_Damages_Bonus);
            put(StatsEnum.Add_Push_Damages_Reduction, StatsEnum.Sub_Push_Damages_Reduction);
            put(StatsEnum.Add_Critical_Damages, StatsEnum.Sub_Critical_Damages);
            put(StatsEnum.Add_Critical_Damages_Reduction, StatsEnum.Sub_Critical_Damages_Reduction);

            put(StatsEnum.Add_Earth_Damages_Bonus, StatsEnum.Sub_Earth_Damages_Bonus);
            put(StatsEnum.Add_Fire_Damages_Bonus, StatsEnum.Sub_Fire_Damages_Bonus);
            put(StatsEnum.Add_Water_Damages_Bonus, StatsEnum.Sub_Water_Damages_Bonus);
            put(StatsEnum.Add_Air_Damages_Bonus, StatsEnum.Sub_Air_Damages_Bonus);
            put(StatsEnum.Add_Neutral_Damages_Bonus, StatsEnum.Sub_Neutral_Damages_Bonus);

            put(StatsEnum.ActionPoints, StatsEnum.Sub_PA);
            put(StatsEnum.Add_Range, StatsEnum.Sub_Range);
            put(StatsEnum.MovementPoints, StatsEnum.Sub_PM);
            put(StatsEnum.DodgePALostProbability, StatsEnum.Sub_Dodge_PA_Probability);
            put(StatsEnum.DodgePMLostProbability, StatsEnum.Sub_Dodge_PM_Probability);
            put(StatsEnum.Strength, StatsEnum.Sub_Strength);
            put(StatsEnum.Intelligence, StatsEnum.Sub_Intelligence);
            put(StatsEnum.Chance, StatsEnum.Sub_Chance);
            put(StatsEnum.Wisdom, StatsEnum.Sub_Wisdom);
            put(StatsEnum.Agility, StatsEnum.Sub_Agility);
            put(StatsEnum.Vitality, StatsEnum.Sub_Vitality);
            put(StatsEnum.Add_Heal_Bonus, StatsEnum.Sub_Heal_Bonus);

            put(StatsEnum.Initiative, StatsEnum.Sub_Initiative);
            put(StatsEnum.Prospecting, StatsEnum.Sub_Prospecting);
            put(StatsEnum.AddDamageMagic, StatsEnum.Sub_Damage);
            put(StatsEnum.Add_Damage_Final_Percent, StatsEnum.SubDamageBonusPercent);
            put(StatsEnum.Add_Push_Damages_Bonus, StatsEnum.Sub_Push_Damages_Bonus);
            put(StatsEnum.Add_CriticalHit, StatsEnum.Sub_Critical_Hit);

            put(StatsEnum.Add_TackleBlock, StatsEnum.Sub_TackleBlock);
            put(StatsEnum.Add_TackleEvade, StatsEnum.Sub_TackleEvade);

        }
    };

    public GenericStats() {

    }

    public GenericStats(Player character) {
        this.myStats.put(StatsEnum.ActionPoints, new CharacterBaseCharacteristic(character.level >= 100 ? 7 : 6));
        this.myStats.put(StatsEnum.MovementPoints, new CharacterBaseCharacteristic(3));
        this.myStats.put(StatsEnum.Prospecting, new CharacterBaseCharacteristic((character.breed == BreedEnum.Enutrof ? 120 : 100)));
        this.myStats.put(StatsEnum.AddPods, new CharacterBaseCharacteristic(1000));
        this.myStats.put(StatsEnum.AddSummonLimit, new CharacterBaseCharacteristic(1));
        this.myStats.put(StatsEnum.Initiative, new CharacterBaseCharacteristic(100));

        this.myStats.put(StatsEnum.Vitality, new CharacterBaseCharacteristic(character.vitality));
        this.myStats.put(StatsEnum.Wisdom, new CharacterBaseCharacteristic(character.wisdom));
        this.myStats.put(StatsEnum.Strength, new CharacterBaseCharacteristic(character.strength));
        this.myStats.put(StatsEnum.Intelligence, new CharacterBaseCharacteristic(character.intell));
        this.myStats.put(StatsEnum.Agility, new CharacterBaseCharacteristic(character.agility));
        this.myStats.put(StatsEnum.Chance, new CharacterBaseCharacteristic(character.chance));
    }

    public Map<StatsEnum, CharacterBaseCharacteristic> getEffects() {
        return this.myStats;
    }

    public void merge(GenericStats stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> Effect : stats.getEffects().entrySet()) {
            if (!this.myStats.containsKey(Effect.getKey())) {
                this.myStats.put(Effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.myStats.get(Effect.getKey()).Merge(Effect.getValue());
        }
    }

    public void unMerge(GenericStats Stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> Effect : Stats.getEffects().entrySet()) {
            {
                if (!this.myStats.containsKey(Effect.getKey())) {
                    this.myStats.put(Effect.getKey(), new CharacterBaseCharacteristic());
                }

                this.myStats.get(Effect.getKey()).UnMerge(Effect.getValue());
            }
        }
    }

    public void reset() {
        this.myStats.values().forEach(x -> {
            {
                x.Base = 0;
                x.additionnal = 0;
                x.alignGiftBonus = 0;
                x.contextModif = 0;
                x.objectsAndMountBonus = 0;
            }
        });
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
            case DodgePALostProbability:
            case DodgePMLostProbability:
            case Add_RETRAIT_PA:
            case Add_RETRAIT_PM:
                total += getTotal(StatsEnum.Wisdom) / 4;
                break;
            case ActionPoints:
                if (isCharacterFighter && (total - myStats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pa")) {
                    total -= (total - myStats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pa");
                }
                total += getTotal(StatsEnum.AddPABis);
                break;
            case MovementPoints:
                if (isCharacterFighter && (total - myStats.get(effectType).additionnal) > DAO.getSettings().getIntElement("Limit.Pm")) {
                    total -= (total - myStats.get(effectType).additionnal) - DAO.getSettings().getIntElement("Limit.Pm");
                }
                total += getTotal(StatsEnum.Add_PM);
                break;
            /*case Reflect:
             Total += getTotal(StatsEnum.AddRenvoiDamageItem);
             break;*/
        }

        // malus ?
        if (OPPOSITE_STATS.containsKey(effectType)) {
            total -= this.getTotal(OPPOSITE_STATS.get(effectType));
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
        if (!this.myStats.containsKey(effectType)) {
            this.myStats.put(effectType, new CharacterBaseCharacteristic(value, 0, 0, 0, 0));
        } else {
            this.myStats.get(effectType).Base += value;
        }
    }

    public void resetBase(){
        this.myStats.values().forEach(stat -> stat.Base = 0);
    }

    public void addBoost(StatsEnum effectType, int value) {
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
             return this.myStats.get(EffectType).Base + getTotal(StatsEnum.strength) + getTotal(StatsEnum.chance) + getTotal(StatsEnum.intelligence) + getTotal(StatsEnum.agility);*/
            default:
                return this.myStats.get(effectType).Base;
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
