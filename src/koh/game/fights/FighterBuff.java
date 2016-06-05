package koh.game.fights;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffState;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellableEffectMessage;
import koh.utils.Couple;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class FighterBuff {

    public List<Couple<EffectCast, Integer>> delayedEffects = new CopyOnWriteArrayList<>();
    private static final Logger logger = LogManager.getLogger(FighterBuff.class);

    @Getter
    private ConcurrentHashMap<BuffActiveType, List<BuffEffect>> buffsAct = new ConcurrentHashMap<BuffActiveType, List<BuffEffect>>() {
        {
            this.put(BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACKED_POST_JET, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACKED_POST_JET_TRAP, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACK_AFTER_JET, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ATTACK_POST_JET, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_HEAL_AFTER_JET, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_BEGINTURN, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ENDTURN, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_ENDMOVE, new ArrayList<>());
            this.put(BuffActiveType.ACTIVE_STATS, new CopyOnWriteArrayList<>());
            this.put(BuffActiveType.ACTIVE_ON_DIE, new CopyOnWriteArrayList<>());
        }
    };

    @Getter
    private ConcurrentHashMap<BuffDecrementType, CopyOnWriteArrayList<BuffEffect>> buffsDec = new ConcurrentHashMap<BuffDecrementType, CopyOnWriteArrayList<BuffEffect>>(3) {
        {
            this.put(BuffDecrementType.TYPE_BEGINTURN, new CopyOnWriteArrayList<>());
            this.put(BuffDecrementType.TYPE_ENDTURN, new CopyOnWriteArrayList<>());
            this.put(BuffDecrementType.TYPE_ENDMOVE, new CopyOnWriteArrayList<>());

        }
    };

    public final Stream<BuffEffect> getAllBuffs() {
        return Stream.concat(Stream.concat(this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN).stream(), this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN).stream()), this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE).stream());
    }

    public boolean buffMaxStackReached(BuffEffect buff) { //CLEARCODE : Distinct state ?
        return buff.castInfos.spellLevel != null && buff.castInfos.spellLevel.getMaxStack() > 0
                && buff.castInfos.spellLevel.getMaxStack()
                <= (buff instanceof BuffState
                        ? this.getAllBuffs().filter(x -> x.castInfos.spellId == buff.castInfos.spellId && x instanceof BuffState && ((BuffState) x).castInfos.effect.value == buff.castInfos.effect.value).count()
                        : this.getAllBuffs().filter(x -> x.castInfos.spellId == buff.castInfos.spellId && x.castInfos.effectType == buff.castInfos.effectType).count());
    }

    public boolean buffMaxStackReached(EffectCast castInfos) {
        return castInfos.spellLevel != null && castInfos.spellLevel.getMaxStack() > 0
                && castInfos.spellLevel.getMaxStack()
                <= (castInfos.effectType == StatsEnum.ADD_STATE
                ? this.getAllBuffs().filter(x -> x.castInfos.spellId == castInfos.spellId && x instanceof BuffState && ((BuffState) x).castInfos.effect.value == castInfos.effect.value).count()
                : this.getAllBuffs().filter(x -> x.castInfos.spellId == castInfos.spellId && x.castInfos.effectType == castInfos.effectType).count());
    }

    public void addBuff(BuffEffect buff) {
        /*if (Buff.getDelay > 0) {
         this.buffsDec.get(BuffDecrementType.TYPE_ENDDELAY).add(Buff);
         return;
         }*/
        if (buffMaxStackReached(buff)) {  //Vue que ces effets s'activent auto à leur lancement
            logger.debug("Buff {} canceled due to stack",buff.getClass().getName());
            return;
        }
        this.buffsAct.get(buff.activeType).add(buff);
        this.buffsDec.get(buff.decrementType).add(buff);
        buff.target.getFight().sendToField(new GameActionFightDispellableEffectMessage(/*Buff.castInfos.effect.effectId*/buff.castInfos.effectType.value(), buff.caster.getID(), buff.getAbstractFightDispellableEffect()));
        logger.debug("Buff {} added",buff,getClass().getName());
    }

    //Le -1 definie l'infini
    public int beginTurn() {
        final MutableInt damage = new MutableInt(0);
        for (Couple<EffectCast, Integer> EffectCast : this.delayedEffects) {
            EffectCast.second--;
            if (EffectCast.second <= 0) {
                this.delayedEffects.remove(EffectCast);
                EffectCast.first.targets.removeIf(Fighter -> !Fighter.isAlive());
                if (EffectBase.tryApplyEffect(EffectCast.first) == -3) {
                    return -3;
                }
            }
        }

        /*for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDDELAY)) {
         Buff.getDelay--;
         if (Buff.getDelay <= 0) {
         this.buffsDec.get(BuffDecrementType.TYPE_ENDDELAY).remove(Buff);
         if (buffMaxStackReached(Buff)) {
         continue;
         }
         this.buffsAct.get(Buff.activeType).add(Buff);
         this.buffsDec.get(Buff.decrementType).add(Buff);
         Buff.target.fight.sendToField(new GameActionFightDispellableEffectMessage(Buff.castInfos.effectType.value(), Buff.caster.id, Buff.getAbstractFightDispellableEffect()));

         }
         }*/
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_BEGINTURN)) {
            if (Buff.applyEffect(damage, null) == -3) {
                return -3;
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.duration != -1 && Buff.decrementDuration() <= 0) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.duration <= 0 && x.duration != -1);

        this.buffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(Buff -> Buff.decrementType == BuffDecrementType.TYPE_BEGINTURN && Buff.duration <= 0);
        });

        return -1;
    }

    public int endTurn() {
        final MutableInt Damage = new MutableInt(0);
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ENDTURN)) {
            if (Buff.applyEffect(Damage, null) == -3) {
                return -3;
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.duration != -1 && Buff.decrementDuration() <= 0) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.duration <= 0 && x.duration != -1);

        for (List<BuffEffect> buffList : this.buffsAct.values()) {
            buffList.removeIf(Buff -> Buff.decrementType == BuffDecrementType.TYPE_ENDTURN && Buff.duration <= 0);
        }

        return -1;
    }

    public int endMove() {
        MutableInt Damage = new MutableInt(0);
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ENDMOVE)) {
            if (Buff.applyEffect(Damage, null) == -3) {
                return -3;
            }
        }

        this.buffsAct.get(BuffActiveType.ACTIVE_ENDMOVE).removeIf(x -> x.decrementType == BuffDecrementType.TYPE_ENDMOVE && x.duration == 0);

        return -1;
    }

    /// <summary>
    /// Lance un soin, activation des buffs d'attaque avant le calcul du jet avec les statistiques
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onHealPostJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_HEAL_AFTER_JET)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }

        return -1;
    }

    /// <summary>
    /// Lance une attaque, activation des buffs d'attaque avant le calcul du jet avec les statistiques
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onAttackPostJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ATTACK_POST_JET)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }

        return -1;
    }

    /// <summary>
    /// Lance une attaque, activation des buffs d'attaque apres le calcul du jet avec les statistiques
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onAttackAfterJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ATTACK_AFTER_JET)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    /// Subit des dommages, activation des buffs de reduction, renvois, anihilation des dommages avant le calcul du jet
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onAttackedPostJet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ATTACKED_POST_JET)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }
    
     /// Subit des dommages, activation des buffs de reduction, renvois, anihilation des dommages avant le calcul du jet
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onAttackedPostJetTrap(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ATTACKED_POST_JET_TRAP)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    /// Subit des dommages, activation des buffs de reduction, renvois, anihilation des dommages apres le calcul du jet
    /// </summary>
    /// <param name="castInfos"></param>
    /// <param name="damageValue"></param>
    public int onAttackedAfterjet(EffectCast CastInfos, MutableInt DamageValue) {
        for (BuffEffect Buff : buffsAct.get(BuffActiveType.ACTIVE_ATTACKED_AFTER_JET)) {
            if (Buff.applyEffect(DamageValue, CastInfos) == -3) {
                return -3;
            }
        }
        return -1;
    }

    public int decrementEffectDuration(int duration) {
        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.isDebuffable() && Buff.decrementDuration(duration) <= 0) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.isDebuffable() && Buff.decrementDuration(duration) <= 0) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE)) {
            if (Buff.isDebuffable() && Buff.decrementDuration(duration) <= 0) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.isDebuffable() && x.duration <= 0);
        this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.isDebuffable() && x.duration <= 0);
        this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE).removeIf(x -> x.isDebuffable() && x.duration <= 0);


        this.buffsAct.values().stream().forEach((BuffList) -> {
            BuffList.removeIf(x -> x.isDebuffable() && x.duration <= 0);
        });

        return -1;
    }

    public int dispell(int spell) {
        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (Buff.castInfos != null && Buff.castInfos.spellId == spell) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (Buff.castInfos != null && Buff.castInfos.spellId == spell) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect Buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE)) {
            if (Buff.castInfos != null && Buff.castInfos.spellId == spell) {
                if (Buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.castInfos != null && x.castInfos.spellId == spell);
        this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.castInfos != null && x.castInfos.spellId == spell);
        this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE).removeIf(x -> x.castInfos != null && x.castInfos.spellId == spell);

        this.buffsAct.values().stream().forEach((buffList) -> {
            buffList.removeIf(x -> x.castInfos != null && x.castInfos.spellId == spell);
        });

        return -1;
    }

    public int debuff() {
        for (BuffEffect buff : this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN)) {
            if (buff.isDebuffable()) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN)) {
            if (buff.isDebuffable()) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        for (BuffEffect buff : this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE)) {
            if (buff.isDebuffable()) {
                if (buff.removeEffect() == -3) {
                    return -3;
                }
            }
        }

        this.buffsDec.get(BuffDecrementType.TYPE_BEGINTURN).removeIf(x -> x.isDebuffable());
        this.buffsDec.get(BuffDecrementType.TYPE_ENDTURN).removeIf(x -> x.isDebuffable());
        this.buffsDec.get(BuffDecrementType.TYPE_ENDMOVE).removeIf(x -> x.isDebuffable());

        this.buffsAct.values().stream().forEach((buffList) -> {
            buffList.removeIf(x -> x.isDebuffable());
        });

        return -1;
    }

    public int debuff(BuffEffect buff){
        if (buff.removeEffect() == -3)
            return -3;

        this.buffsDec.get(buff.decrementType).remove(buff);
        this.buffsAct.get(buff.activeType).remove(buff);

        return -1;
    }

}
