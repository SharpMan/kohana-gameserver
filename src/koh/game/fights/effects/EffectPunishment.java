package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamagePerAttackantLife;
import koh.game.fights.effects.buff.BuffReflectSpell;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.SpellIDEnum;
import koh.protocol.client.enums.StatsEnum;
import static koh.protocol.client.enums.StatsEnum.DAMAGE_NEUTRAL;

import koh.protocol.messages.game.actions.fight.*;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectPunishment extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        // Si > 0 alors c'est un buff
        if (castInfos.duration > 0) {
            // L'effet est un poison
            castInfos.isPoison = true;

            // Ajout du buff
            castInfos.targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffDamagePerAttackantLife(castInfos, Target));
            });
        } else // Dommage direct
        {
            for (Fighter Target : castInfos.targets) {
                //Eppe de iop ?
                MutableInt DamageValue = new MutableInt(castInfos.randomJet(Target));

                if (applyDamages(castInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
            if(castInfos.spellId == SpellIDEnum.MASCARADE
                    || castInfos.spellId == SpellIDEnum.MASCARADE_DOPEUL
                    || castInfos.spellId == SpellIDEnum.MASCARADE_ROCAMBOLESQUE){
                MutableInt DamageValue = new MutableInt(castInfos.randomJet(castInfos.caster));

                if (applyDamages(castInfos, castInfos.caster, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static final int applyDamages(EffectCast castInfos, Fighter target, MutableInt damageJet) {

        if (target.getStates().hasState(FightStateEnum.STATE_REFLECT_SPELL) && !castInfos.isPoison && ((BuffReflectSpell) target.getStates().getBuffByState(FightStateEnum.STATE_REFLECT_SPELL)).reflectLevel >= castInfos.spellLevel.getGrade()) {
            target.getFight().sendToField(new GameActionFightReflectSpellMessage(ActionIdEnum.ACTION_CHARACTER_SPELL_REFLECTOR, target.getID(), castInfos.caster.getID()));
            target = castInfos.caster;
        }
        Fighter Caster = castInfos.caster;
        // Perd l'invisibilité s'il inflige des dommages direct
        if (!castInfos.isPoison && !castInfos.isTrap && !castInfos.isReflect) {
            Caster.getStates().removeState(FightStateEnum.Invisible);
        }

        // Application des buffs avant calcul totaux des dommages, et verification qu'ils n'entrainent pas la fin du combat
        if (!castInfos.isPoison && !castInfos.isReflect) {
            if (Caster.getBuff().onAttackPostJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
            if (target.getBuff().onAttackedPostJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        double num1 = 0.0;
        double num2 = (double) castInfos.caster.getLife() / (double) castInfos.caster.getMaxLife();
        if (num2 <= 0.5) {
            num1 = 2.0 * num2;
        } else if (num2 > 0.5) {
            num1 = 1.0 + (num2 - 0.5) * -2.0;
        }
        damageJet.setValue((double) castInfos.caster.getLife() * num1 * (double) castInfos.randomJet(target) / 100.0);

        if(castInfos.caster.hasState(FightStateEnum.PACIFISTE.value) && !castInfos.isGlyph){ //Une glyphe qui punit .. beh
            damageJet.setValue(0);
        }

        // Calcul resistances
        target.calculReduceDamages(DAMAGE_NEUTRAL, damageJet);
        // Reduction des dommages grace a l'armure
        if (damageJet.intValue() > 0) {
            // Si ce n'est pas des dommages direct on ne reduit pas
            if (!castInfos.isPoison && !castInfos.isReflect) {
                // Calcul de l'armure par rapport a l'effet
                int Armor = target.calculArmor(DAMAGE_NEUTRAL);
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
        if (!castInfos.isPoison && !castInfos.isReflect) {
            if (Caster.getBuff().onAttackAfterJet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
            if (target.getBuff().onAttackedAfterjet(castInfos, damageJet) == -3) {
                return -3; // Fin du combat
            }
        }

        // S'il subit des dommages
        if (damageJet.getValue() > 0) {
            // Si c'est pas un poison ou un renvoi on applique le renvoie
            if (!castInfos.isPoison && !castInfos.isReflect) {
                MutableInt reflectDamage = new MutableInt(target.getReflectedDamage());

                // Si du renvoi
                if (reflectDamage.intValue() > 0 && target.getID() != Caster.getID()) {
                    target.getFight().sendToField(new GameActionFightReflectDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_REFLECTOR, target.getID(), Caster.getID()));

                    // Trop de renvois
                    if (reflectDamage.getValue() > damageJet.getValue()) {
                        reflectDamage.setValue(damageJet.getValue());
                    }

                    EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, 0, (short) 0, 0, null, target, null, false, StatsEnum.NONE, 0, null);
                    SubInfos.isReflect = true;

                    // Si le renvoi de dommage entraine la fin de combat on stop
                    if (EffectDamage.applyDamages(SubInfos, Caster, reflectDamage) == -3) {
                        return -3;
                    }

                    // Dommage renvoyé
                    damageJet.add(-reflectDamage.intValue());
                }
            }
        }
        // Peu pas etre en dessous de 0
        if (damageJet.getValue() < 0) {
            damageJet.setValue(0);
        }

        // Dommages superieur a la vie de la cible
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
