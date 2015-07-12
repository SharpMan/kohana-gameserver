package koh.game.fights;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import koh.game.Main;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellableEffectMessage;
import koh.utils.Couple;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class FighterBuff {

    public List<Couple<EffectCast, Integer>> DelayedEffects = new CopyOnWriteArrayList<>();

    private HashMap<BuffActiveType, ArrayList<BuffEffect>> BuffsAct = new HashMap<BuffActiveType, ArrayList<BuffEffect>>() {
        {
            this.put(BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACKED_POST_JET, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACK_AFTER_JET, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACK_POST_JET, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_BEGINTURN, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ENDTURN, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ENDMOVE, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_STATS, new ArrayList<>());
        }
    };

    private HashMap<BuffDecrementType, ArrayList<BuffEffect>> BuffsDec = new HashMap<BuffDecrementType, ArrayList<BuffEffect>>() {
        {
            this.put(BuffDecrementType.TYPE_BEGINTURN, new ArrayList<>());
            this.put(BuffDecrementType.TYPE_ENDTURN, new ArrayList<>());
            this.put(BuffDecrementType.TYPE_ENDMOVE, new ArrayList<>());

        }
    };

    public Stream<BuffEffect> GetAllBuffs() {
        return Stream.concat(Stream.concat(this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN).stream(), this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN).stream()), this.BuffsDec.get(BuffDecrementType.TYPE_ENDMOVE).stream());
    }

    public boolean BuffMaxStackReached(BuffEffect Buff) { //CLEARCODE : Distinct state ?
        return Buff.CastInfos.SpellLevel != null && Buff.CastInfos.SpellLevel.maxStack > 0
                && Buff.CastInfos.SpellLevel.maxStack
                <= (Buff instanceof BuffState
                        ? this.GetAllBuffs().filter(x -> x.CastInfos.SpellId == Buff.CastInfos.SpellId && x instanceof BuffState && ((BuffState) x).CastInfos.Effect.value == Buff.CastInfos.Effect.value).count()
                        : this.GetAllBuffs().filter(x -> x.CastInfos.SpellId == Buff.CastInfos.SpellId && x.CastInfos.EffectType == Buff.CastInfos.EffectType).count());
    }

    public void AddBuff(BuffEffect Buff) {
        /*if (Buff.Delay > 0) {
         this.BuffsDec.get(BuffDecrementType.TYPE_ENDDELAY).add(Buff);
         return;
         }*/
        if (BuffMaxStackReached(Buff)) {  //Vue que ces effets s'activent auto à leur lancement 
            Main.Logs().writeDebug("Buff " + Buff.getClass().getName() + " canceled due to stack");
            return;
        }
        this.BuffsAct.get(Buff.ActiveType).add(Buff);
        this.BuffsDec.get(Buff.DecrementType).add(Buff);
        Buff.Target.Fight.sendToField(new GameActionFightDispellableEffectMessage(/*Buff.CastInfos.Effect.effectId*/Buff.CastInfos.EffectType.value(), Buff.Caster.ID, Buff.GetAbstractFightDispellableEffect()));
    }

    //Le -1 definie l'infini
    public int BeginTurn() {
        MutableInt Damage = new MutableInt(0);
        for (Couple<EffectCast, Integer> EffectCast : this.DelayedEffects) {
            EffectCast.second--;
            if (EffectCast.second <= 0) {
                this.DelayedEffects.remove(EffectCast);
                if (EffectBase.TryApplyEffect(EffectCast.first) == -3) {
                    return -3;
                }
            }
        }

        /*for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_ENDDELAY)) {
         Buff.Delay--;
         if (Buff.Delay <= 0) {
         this.BuffsDec.get(BuffDecrementType.TYPE_ENDDELAY).remove(Buff);
         if (BuffMaxStackReached(Buff)) {
         continue;
         }
         this.BuffsAct.get(Buff.ActiveType).add(Buff);
         this.BuffsDec.get(Buff.DecrementType).add(Buff);
         Buff.Target.Fight.sendToField(new GameActionFightDispellableEffectMessage(Buff.CastInfos.EffectType.value(), Buff.Caster.ID, Buff.GetAbstractFightDispellableEffect()));

         }
         }*/
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_BEGINTURN)) {
            if (Buff.ApplyEffect(Damage, null) == -3) {
                return -3;
            }
        }

        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.Duration != -1 && Buff.DecrementDuration() <= 0) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.Duration <= 0 && x.Duration != -1);

        this.BuffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(Buff -> Buff.DecrementType == BuffDecrementType.TYPE_BEGINTURN && Buff.Duration <= 0);
        });

        return -1;
    }

    public int EndTurn() {
        MutableInt Damage = new MutableInt(0);
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ENDTURN)) {
            if (Buff.ApplyEffect(Damage, null) == -3) {
                return -3;
            }
        }

        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.Duration != -1 && Buff.DecrementDuration() <= 0) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.Duration <= 0 && x.Duration != -1);

        for (ArrayList<BuffEffect> BuffList : this.BuffsAct.values()) {
            BuffList.removeIf(Buff -> Buff.DecrementType == BuffDecrementType.TYPE_ENDTURN && Buff.Duration <= 0);
        }

        return -1;
    }

    public int EndMove() {
        MutableInt Damage = new MutableInt(0);
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ENDMOVE)) {
            if (Buff.ApplyEffect(Damage, null) == -3) {
                return -3;
            }
        }

        this.BuffsAct.get(BuffActiveType.ACTIVE_ENDMOVE).removeIf(x -> x.DecrementType == BuffDecrementType.TYPE_ENDMOVE && x.Duration == 0);

        return -1;
    }

    /// <summary>
    /// Lance une attaque, activation des buffs d'attaque avant le calcul du jet avec les statistiques
    /// </summary>
    /// <param name="CastInfos"></param>
    /// <param name="DamageValue"></param>
    public int OnAttackPostJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ATTACK_POST_JET)) {
            if (Buff.ApplyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }

        return -1;
    }

    /// <summary>
    /// Lance une attaque, activation des buffs d'attaque apres le calcul du jet avec les statistiques
    /// </summary>
    /// <param name="CastInfos"></param>
    /// <param name="DamageValue"></param>
    public int OnAttackAfterJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ATTACK_AFTER_JET)) {
            if (Buff.ApplyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    /// Subit des dommages, activation des buffs de reduction, renvois, anihilation des dommages avant le calcul du jet
    /// </summary>
    /// <param name="CastInfos"></param>
    /// <param name="DamageValue"></param>
    public int OnAttackedPostJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ATTACKED_POST_JET)) {
            if (Buff.ApplyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    /// Subit des dommages, activation des buffs de reduction, renvois, anihilation des dommages apres le calcul du jet
    /// </summary>
    /// <param name="CastInfos"></param>
    /// <param name="DamageValue"></param>
    public int OnAttackedAfterJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : BuffsAct.get(BuffActiveType.ACTIVE_ATTACKED_AFTER_JET)) {
            if (Buff.ApplyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public int DecrementEffectDuration(int Duration) {
        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.IsDebuffable() && Buff.DecrementDuration(Duration) <= 0) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.IsDebuffable() && Buff.DecrementDuration(Duration) <= 0) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.IsDebuffable() && x.Duration <= 0);
        this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.IsDebuffable() && x.Duration <= 0);

        this.BuffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(x -> x.IsDebuffable() && x.Duration <= 0);
        });

        return -1;
    }

    public int Dispell(int Spell) {
        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.CastInfos != null && Buff.CastInfos.SpellId == Spell) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.CastInfos != null && Buff.CastInfos.SpellId == Spell) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.CastInfos != null && x.CastInfos.SpellId == Spell);
        this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.CastInfos != null && x.CastInfos.SpellId == Spell);

        this.BuffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(x -> x.CastInfos != null && x.CastInfos.SpellId == Spell);
        });

        return -1;
    }

    public int Debuff() {
        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.IsDebuffable()) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.IsDebuffable()) {
                if (Buff.RemoveEffect() == -3) {
                    return -3;
                }
            }
        }

        this.BuffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.IsDebuffable());
        this.BuffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.IsDebuffable());

        this.BuffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(x -> x.IsDebuffable());
        });

        return -1;
    }

}
