package koh.game.fights.effects;

import java.util.ArrayList;
import java.util.Random;
import koh.game.entities.spells.EffectInstanceDice;
import koh.game.entities.spells.SpellLevel;
import koh.game.fights.Fight;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffMaximiseEffects;
import koh.game.fights.effects.buff.BuffMinimizeEffects;
import koh.protocol.client.enums.StatsEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class EffectCast {

    private static final Random EFFECT_RANDOM = new Random();

    public static boolean isDamageEffect(StatsEnum EffectType) {
        switch (EffectType) {
            case STEAL_EARTH:
            case STEAL_WATER:
            case STEAL_AIR:
            case STEAL_FIRE:
            case STEAL_NEUTRAL:
            case DAMAGE_WATER:
            case DAMAGE_EARTH:
            case DAMAGE_AIR:
            case DAMAGE_FIRE:
            case DAMAGE_NEUTRAL:
                return true;
        }

        return false;
    }

    public StatsEnum effectType;
    public StatsEnum subEffect;
    public EffectInstanceDice effect;

    public int getEffectUID() {
        if (this.effect != null) {
            return this.effect.effectUid;
        }
        return 0;
    }

    public int spellId, fakeValue, damageValue, parentUID, glyphId;
    public short cellId,oldCell,targetKnownCellId;
    public boolean isReflect, isPoison, isCAC, isTrap, isGlyph, isReturnedDamages,isPoutch;
    @Getter @Setter
    private boolean critical;
    public double chance;
    public SpellLevel spellLevel;
    public Fighter caster;
    public ArrayList<Fighter> targets;
    public int duration;

    public Fight getFight(){
        return this.caster.getFight();
    }

    public static int randomValue(int i1, int i2) {
        //random rand = new random();
        return EFFECT_RANDOM.nextInt(i2 - i1 + 1) + i1;
    }

    /* public short MaxEffect() {
     return (short) (this.effect.diceNum >= (int) this.effect.diceSide ? this.effect.diceNum : this.effect.diceSide);
     }*/
    public short randomJet(Fighter Target) {
        if (this.effect == null) {
            return (short) this.damageValue;
        }

        int num1 = this.effect.diceNum >= (int) this.effect.diceSide ? this.effect.diceNum : this.effect.diceSide;
        int num2 = this.effect.diceNum <= (int) this.effect.diceSide ? this.effect.diceNum : this.effect.diceSide;

        /*if (type == EffectGenerationType.MAX_EFFECTS) {
         return  new EffectInteger(this.id, this.getTemplate.operator != "-" ? num1 : num2, (EffectBase) this);
         }
         if (type == EffectGenerationType.MIN_EFFECTS) {
         return new EffectInteger(this.id, this.getTemplate.operator != "-" ? num2 : num1, (EffectBase) this);
         }*/
        if(isCAC && fakeValue == -1){
            return (short) num1;
        }
        if ((int) num2 == 0) {
            return (short) num1;
        } else {
            if (Target.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMaximiseEffects)) {
                return (short) num1;
            }
            if (caster.getBuff().getAllBuffs().anyMatch(x -> x instanceof BuffMinimizeEffects)) {
                return (short) num2;
            }
            return (short) randomValue(num2, num1);
        }
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets) {
        this.effectType = EffectType;
        this.spellId = SpellId;
        this.cellId = CellId;
        this.chance = Chance;
        this.caster = Caster;
        this.targets = Targets;
        this.effect = Effect;
        this.subEffect = StatsEnum.NONE;
        this.damageValue = 0;
        this.isCAC = false;
        this.spellLevel = null;
        this.duration = Effect != null ? Effect.duration : 0;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, SpellLevel sl) {
        this.effectType = EffectType;
        this.spellId = SpellId;
        this.cellId = CellId;
        this.chance = Chance;
        this.caster = Caster;
        this.targets = Targets;
        this.effect = Effect;
        this.subEffect = StatsEnum.NONE;
        this.damageValue = 0;
        this.isCAC = false;
        this.spellLevel = null;
        this.duration = Effect != null ? Effect.duration : 0;
        this.spellLevel = sl;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, boolean IsCAC, StatsEnum SubEffect, int DamageValue, SpellLevel sl) {
        this.effectType = EffectType;
        this.spellId = SpellId;
        this.cellId = CellId;
        this.chance = Chance;
        this.caster = Caster;
        this.targets = Targets;
        this.effect = Effect;
        this.subEffect = SubEffect;
        this.damageValue = DamageValue;
        this.isCAC = IsCAC;
        this.spellLevel = sl;
        this.duration = Effect != null ? Effect.duration : 0;
    }

    public int getDelay() {
        if (this.effect != null) {
            return this.effect.delay;
        }
        return 0;
    }

    public EffectCast(StatsEnum EffectType, int SpellId, short CellId, double Chance, EffectInstanceDice Effect, Fighter Caster, ArrayList<Fighter> Targets, boolean IsCAC, StatsEnum SubEffect, int DamageValue, SpellLevel sl, int Duration, int ParentUID) {
        this.effectType = EffectType;
        this.spellId = SpellId;
        this.cellId = CellId;
        this.chance = Chance;
        this.caster = Caster;
        this.targets = Targets;
        this.effect = Effect;
        this.subEffect = SubEffect;
        this.damageValue = DamageValue;
        this.isCAC = IsCAC;
        this.spellLevel = sl;
        this.duration = Duration;
        this.parentUID = ParentUID;
    }
    
    public FightCell getCell(){
        return this.caster.getFight().getCell(cellId);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
