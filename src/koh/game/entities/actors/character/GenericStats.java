package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import koh.game.entities.actors.Player;
import koh.game.utils.Settings;
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
            put(StatsEnum.Add_Damage_Bonus_Percent, StatsEnum.SubDamageBonusPercent);
            put(StatsEnum.Add_Push_Damages_Bonus, StatsEnum.Sub_Push_Damages_Bonus);
            put(StatsEnum.Add_CriticalHit, StatsEnum.Sub_Critical_Hit);

            put(StatsEnum.Add_TackleBlock, StatsEnum.Sub_TackleBlock);
            put(StatsEnum.Add_TackleEvade, StatsEnum.Sub_TackleEvade);

        }
    };

    public GenericStats() {

    }

    public GenericStats(Player Character) {
        this.myStats.put(StatsEnum.ActionPoints, new CharacterBaseCharacteristic(Character.Level >= 100 ? 7 : 6));
        this.myStats.put(StatsEnum.MovementPoints, new CharacterBaseCharacteristic(3));
        this.myStats.put(StatsEnum.Prospecting, new CharacterBaseCharacteristic((Character.Breed == BreedEnum.Enutrof ? 120 : 100)));
        this.myStats.put(StatsEnum.AddPods, new CharacterBaseCharacteristic(1000));
        this.myStats.put(StatsEnum.AddSummonLimit, new CharacterBaseCharacteristic(1));
        this.myStats.put(StatsEnum.Initiative, new CharacterBaseCharacteristic(100));

        this.myStats.put(StatsEnum.Vitality, new CharacterBaseCharacteristic(Character.Vitality));
        this.myStats.put(StatsEnum.Wisdom, new CharacterBaseCharacteristic(Character.Wisdom));
        this.myStats.put(StatsEnum.Strength, new CharacterBaseCharacteristic(Character.Strength));
        this.myStats.put(StatsEnum.Intelligence, new CharacterBaseCharacteristic(Character.Intell));
        this.myStats.put(StatsEnum.Agility, new CharacterBaseCharacteristic(Character.Agility));
        this.myStats.put(StatsEnum.Chance, new CharacterBaseCharacteristic(Character.Chance));
    }

    public Map<StatsEnum, CharacterBaseCharacteristic> GetEffects() {
        return this.myStats;
    }

    public void Merge(GenericStats Stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> Effect : Stats.GetEffects().entrySet()) {
            if (!this.myStats.containsKey(Effect.getKey())) {
                this.myStats.put(Effect.getKey(), new CharacterBaseCharacteristic());
            }
            this.myStats.get(Effect.getKey()).Merge(Effect.getValue());
        }
    }

    public void UnMerge(GenericStats Stats) {
        for (Entry<StatsEnum, CharacterBaseCharacteristic> Effect : Stats.GetEffects().entrySet()) {
            {
                if (!this.myStats.containsKey(Effect.getKey())) {
                    this.myStats.put(Effect.getKey(), new CharacterBaseCharacteristic());
                }

                this.myStats.get(Effect.getKey()).UnMerge(Effect.getValue());
            }
        }
    }

    public void Reset() {
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

    public int GetTotal(StatsEnum EffectType) {
        return GetTotal(EffectType, false);
    }

    public int GetTotal(StatsEnum EffectType, boolean isCharacterFighter) {
        int Total = 0;

        // existant ?
        if (myStats.containsKey(EffectType)) {
            Total += myStats.get(EffectType).Total();
        }

        switch (EffectType) {
            /*case Initiative:
             Total += GetTotal(StatsEnum.Strength) + GetTotal(StatsEnum.Chance) + GetTotal(StatsEnum.Intelligence) + GetTotal(StatsEnum.Agility);*/
            case DodgePALostProbability:
            case DodgePMLostProbability:
            case Add_RETRAIT_PA:
            case Add_RETRAIT_PM:
                Total += GetTotal(StatsEnum.Wisdom) / 4;
                break;
            case ActionPoints:
                if (isCharacterFighter && (Total - myStats.get(EffectType).additionnal) > Settings.GetIntElement("Limit.Pa")) {
                    Total -= (Total - myStats.get(EffectType).additionnal) - Settings.GetIntElement("Limit.Pa");
                }
                Total += GetTotal(StatsEnum.AddPABis);
                break;
            case MovementPoints:
                if (isCharacterFighter && (Total - myStats.get(EffectType).additionnal) > Settings.GetIntElement("Limit.Pm")) {
                    Total -= (Total - myStats.get(EffectType).additionnal) - Settings.GetIntElement("Limit.Pm");
                }
                Total += GetTotal(StatsEnum.Add_PM);
                break;
            /*case Reflect:
             Total += GetTotal(StatsEnum.AddRenvoiDamageItem);
             break;*/
        }

        // malus ?
        if (OPPOSITE_STATS.containsKey(EffectType)) {
            Total -= this.GetTotal(OPPOSITE_STATS.get(EffectType));
        }
        return Total;
    }

    public CharacterBaseCharacteristic GetEffect(StatsEnum EffectType) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(EffectType);
    }

    public CharacterBaseCharacteristic GetEffect(int EffectType) {
        return GetEffect(StatsEnum.valueOf(EffectType));
    }

    public void AddBase(StatsEnum EffectType, int Value) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic(Value, 0, 0, 0, 0));
        } else {
            this.myStats.get(EffectType).Base += Value;
        }
    }

    public void AddBoost(StatsEnum EffectType, int Value) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic(0, Value, 0, 0, 0));
        } else {
            this.myStats.get(EffectType).additionnal += Value;
        }
    }

    public int GetBase(StatsEnum EffectType) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic());
        }
        switch (EffectType) {
            /*case Initiative:
             return this.myStats.get(EffectType).Base + GetTotal(StatsEnum.Strength) + GetTotal(StatsEnum.Chance) + GetTotal(StatsEnum.Intelligence) + GetTotal(StatsEnum.Agility);*/
            default:
                return this.myStats.get(EffectType).Base;
        }
    }

    public int GetBoost(StatsEnum EffectType) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(EffectType).additionnal;
    }

    public int GetItem(StatsEnum EffectType) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic());
        }
        return this.myStats.get(EffectType).objectsAndMountBonus;
    }

    public void AddItem(StatsEnum EffectType, int Value) {
        if (!this.myStats.containsKey(EffectType)) {
            this.myStats.put(EffectType, new CharacterBaseCharacteristic(0, 0, Value, 0, 0));
        } else {
            this.myStats.get(EffectType).objectsAndMountBonus += Value;
        }
    }

    public void totalClear() {
        try {
            for (CharacterBaseCharacteristic CB : this.myStats.values()) {
                CB.totalClear();
            }
            myStats.clear();
            myStats = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
