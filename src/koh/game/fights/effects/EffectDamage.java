package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamage;
import koh.game.fights.effects.buff.BuffReflectSpell;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.*;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamage extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            CastInfos.targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffDamage(CastInfos, Target));
            });
        } else // Dommage direct
        {
            for (Fighter Target : CastInfos.targets) {
                //Eppe de iop ?
                MutableInt DamageValue = new MutableInt(CastInfos.randomJet(Target));

                if (EffectDamage.applyDamages(CastInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int applyDamages(EffectCast castInfos, Fighter target, MutableInt damageJet) {

        if (target.getStates().hasState(FightStateEnum.STATE_REFLECT_SPELL) && !castInfos.IsPoison && ((BuffReflectSpell) target.getStates().getBuffByState(FightStateEnum.STATE_REFLECT_SPELL)).reflectLevel >= castInfos.SpellLevel.getGrade()) {
            target.getFight().sendToField(new GameActionFightReflectSpellMessage(ActionIdEnum.ACTION_CHARACTER_SPELL_REFLECTOR, target.getID(), castInfos.caster.getID()));
            target = castInfos.caster;
        }
        Fighter Caster = castInfos.caster;
        // Perd l'invisibilité s'il inflige des dommages direct
        if (!castInfos.IsPoison && !castInfos.IsTrap && !castInfos.IsReflect) {
            Caster.getStates().removeState(FightStateEnum.Invisible);
        }

        // Application des buffs avant calcul totaux des dommages, et verification qu'ils n'entrainent pas la fin du combat
        if (!castInfos.IsPoison && !castInfos.IsReflect) {
            if (Caster.getBuff().onAttackPostJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
            if (target.getBuff().onAttackedPostJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        if(!castInfos.IsReflect && castInfos.IsTrap){
            if (target.getBuff().onAttackedPostJetTrap(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        // Calcul jet
        Caster.computeDamages(castInfos.EffectType, damageJet);
        //Calcul Bonus Negatif Zone ect ...
        if (castInfos.effect != null) {
            Caster.calculBonusDamages(castInfos.effect, damageJet,castInfos.CellId , target.getCellId(),castInfos.oldCell);
        }

        // Calcul resistances
        target.calculReduceDamages(castInfos.EffectType, damageJet);
        // Reduction des dommages grace a l'armure
        if (damageJet.intValue() > 0) {
            // Si ce n'est pas des dommages direct on ne reduit pas
            if (!castInfos.IsPoison && !castInfos.IsReflect && castInfos.EffectType != StatsEnum.DamageBrut) {
                // Calcul de l'armure par rapport a l'effet
                int Armor = target.calculArmor(castInfos.EffectType);
                // Si il reduit un minimum
                if (Armor != 0) {
                    // XX Reduit les dommages de X

                    target.getFight().sendToField(new GameActionFightReduceDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_MODERATOR, target.getID(), target.getID(), Armor));

                    // On reduit
                    damageJet.setValue(damageJet.intValue() - Armor);

                    // Si on suprimme totalement les dommages
                    if (damageJet.intValue() < 0) {
                        damageJet.setValue(0);
                    }
                }
            }
        }
        // Application des buffs apres le calcul totaux et l'armure
        if (!castInfos.IsPoison && !castInfos.IsReflect) {
            if (Caster.getBuff().onAttackAfterJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
            if (target.getBuff().onattackedafterjet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
        }

        // S'il subit des dommages
        if (damageJet.getValue() > 0) {
            // Si c'est pas un poison ou un renvoi on applique le renvoie
            if (!castInfos.IsPoison && !castInfos.IsReflect) {
                MutableInt ReflectDamage = new MutableInt(target.getReflectedDamage());

                // Si du renvoi
                if (ReflectDamage.intValue() > 0 && target.getID() != Caster.getID()) {
                    target.getFight().sendToField(new GameActionFightReflectDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_REFLECTOR, target.getID(), Caster.getID()));

                    // Trop de renvois
                    if (ReflectDamage.getValue() > damageJet.getValue()) {
                        ReflectDamage.setValue(damageJet.getValue());
                    }

                    EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, 0, (short) 0, 0, null, target, null, false, StatsEnum.NONE, 0, null);
                    SubInfos.IsReflect = true;

                    // Si le renvoi de dommage entraine la fin de combat on stop
                    if (EffectDamage.applyDamages(SubInfos, Caster, ReflectDamage) == -3) {
                        return -3;
                    }

                    // Dommage renvoyé
                    damageJet.add(-ReflectDamage.intValue());
                }
            }
        }
        // Peu pas etre en dessous de 0
        if (damageJet.getValue() < 0) {
            damageJet.setValue(0);
        }

        // Dommages superieur a la vie de la cible
        if (damageJet.getValue() > target.getLife() + target.getShieldPoints()) {
            damageJet.setValue(target.getLife() + target.getShieldPoints());
        }

        // On verifie les point bouclier d'abord
        if(target.getShieldPoints() > 0){
            if(target.getShieldPoints() > damageJet.intValue()){
                target.setShieldPoints(target.getShieldPoints() - damageJet.getValue());
                target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, Caster.getID(), target.getID(), 0, 0, damageJet.intValue()));
            }
            else{
                int lifePointRemaining = damageJet.toInteger() - target.getShieldPoints();
                target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, Caster.getID(), target.getID(), lifePointRemaining, 0, target.getShieldPoints()));
                target.setLife(target.getLife() - lifePointRemaining);
                target.setShieldPoints(0);
            }
            return target.tryDie(Caster.getID());
        }

        // Deduit la vie
        target.setLife(target.getLife() - damageJet.intValue());

        // Enois du packet combat subit des dommages
        if (damageJet.intValue() != 0) {
            target.getFight().sendToField(new GameActionFightLifePointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, Caster.getID(), target.getID(), damageJet.intValue(), 0));
        }
        return target.tryDie(Caster.getID());
    }

}
