package koh.game.fights.effects;

import java.util.ArrayList;
import java.util.Random;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class EffectCast {

    private static final Random EFFECT_RANDOM = new Random();

    public static boolean IsDamageEffect(StatsEnum EffectType) {
        switch (EffectType) {
            case Steal_Earth:
            case Steal_Water:
            case Steal_Air:
            case Steal_Fire:
            case Steal_Neutral:
            case Damage_Water:
            case Damage_Earth:
            case Damage_Air:
            case Damage_Fire:
            case Damage_Neutral:
                return true;
        }

        return false;
    }

    public StatsEnum EffectType;
    public StatsEnum SubEffect;
    public EffectInstanceDice Effect;

    public int GetEffectUID() {
        if (this.Effect != null) {
            return this.Effect.effectUid;
        }
        return 0;
    }

    public int SpellId, FakeValue, DamageValue, ParentUID, targetKnownCellId;
    public short CellId,oldCell;
    public boolean IsReflect, IsPoison, IsCAC, IsTrap, IsReturnedDamages;
    public double Chance;
    public SpellLevel SpellLevel;
    public Fighter Caster;
    public ArrayList<Fighter> Targets;
    public int Duration;

    public static int RandomValue(int i1, int i2) {
        //Random rand = new Random();
        return EFFECT_RANDOM.nextInt(i2 - i1 + 1) + i1;
    }

    /* public short MaxEffect() {
     return (short) (this.Effect.diceNum >= (int) this.Effect.diceSide ? this.Effect.diceNum : this.Effect.diceSide);
     }*/
    public short RandomJet(Fighter Target) {
        if (this.Effect == null) {
            return (short) this.DamageValue;
        }

        int num1 = this.Effect.diceNum >= (int) this.Effect.diceSide ? this.Effect.diceNum : this.Effect.diceSide;
        int num2 = this.Effect.diceNum <= (int) this.Effect.diceSide ? this.Effect.diceNum : this.Effect.diceSide;

        /*if (type == EffectGenerationType.MaxEffects) {
         return  new EffectInteger(this.id, this.getTemplate.Operator != "-" ? num1 : num2, (EffectBase) this);
         }
         if (type == EffectGenerationType.MinEffects) {
         return new EffectInteger(this.id, this.getTemplate.Operator != "-" ? num2 : num1, (EffectBase) this);
         }*/
        if ((int) num2 == 0) {
            return (short) num1;
        } else {
            if (Target.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMaximiseEffects)) {
                return (short) num1;
            }
            if (Caster.Buffs.GetAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects)) {
                return (short) num2;
            }
            return (short) RandomValue(num2, num1);
        }
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets) {
        this.EffectType = EffectType;
        this.SpellId = SpellId;
        this.CellId = CellId;
        this.Chance = Chance;
        this.Caster = Caster;
        this.Targets = Targets;
        this.Effect = Effect;
        this.SubEffect = StatsEnum.NONE;
        this.DamageValue = 0;
        this.IsCAC = false;
        this.SpellLevel = null;
        this.Duration = Effect != null ? Effect.duration : 0;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, SpellLevel sl) {
        this.EffectType = EffectType;
        this.SpellId = SpellId;
        this.CellId = CellId;
        this.Chance = Chance;
        this.Caster = Caster;
        this.Targets = Targets;
        this.Effect = Effect;
        this.SubEffect = StatsEnum.NONE;
        this.DamageValue = 0;
        this.IsCAC = false;
        this.SpellLevel = null;
        this.Duration = Effect != null ? Effect.duration : 0;
        this.SpellLevel = sl;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, boolean IsCAC, StatsEnum SubEffect, int DamageValue, SpellLevel sl) {
        this.EffectType = EffectType;
        this.SpellId = SpellId;
        this.CellId = CellId;
        this.Chance = Chance;
        this.Caster = Caster;
        this.Targets = Targets;
        this.Effect = Effect;
        this.SubEffect = SubEffect;
        this.DamageValue = DamageValue;
        this.IsCAC = IsCAC;
        this.SpellLevel = sl;
        this.Duration = Effect != null ? Effect.duration : 0;
    }

    public int Delay() {
        if (this.Effect != null) {
            return this.Effect.delay;
        }
        return 0;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, boolean IsCAC, StatsEnum SubEffect, int DamageValue, SpellLevel sl, int Duration, int ParentUID) {
        this.EffectType = EffectType;
        this.SpellId = SpellId;
        this.CellId = CellId;
        this.Chance = Chance;
        this.Caster = Caster;
        this.Targets = Targets;
        this.Effect = Effect;
        this.SubEffect = SubEffect;
        this.DamageValue = DamageValue;
        this.IsCAC = IsCAC;
        this.SpellLevel = sl;
        this.Duration = Duration;
        this.ParentUID = ParentUID;
    }
    
    public FightCell Cell(){
        return this.Caster.Fight.GetCell(CellId);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
