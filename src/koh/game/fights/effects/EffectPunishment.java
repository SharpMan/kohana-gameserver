package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamagePerAttackantLife;
import koh.game.fights.effects.buff.BuffReflectSpell;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import static koh.protocol.client.enums.StatsEnum.Damage_Neutral;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReduceDamagesMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReflectDamagesMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReflectSpellMessage;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectPunishment extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            CastInfos.Targets.stream().forEach((Target) -> {
                Target.buff.addBuff(new BuffDamagePerAttackantLife(CastInfos, Target));
            });
        } else // Dommage direct
        {
            for (Fighter Target : CastInfos.Targets) {
                //Eppe de iop ?
                MutableInt DamageValue = new MutableInt(CastInfos.RandomJet(Target));

                if (ApplyDamages(CastInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int ApplyDamages(EffectCast CastInfos, Fighter Target, MutableInt DamageJet) {

        if (Target.states.hasState(FightStateEnum.STATE_REFLECT_SPELL) && !CastInfos.IsPoison && ((BuffReflectSpell) Target.states.getBuffByState(FightStateEnum.STATE_REFLECT_SPELL)).reflectLevel >= CastInfos.SpellLevel.getGrade()) {
            Target.fight.sendToField(new GameActionFightReflectSpellMessage(ActionIdEnum.ACTION_CHARACTER_SPELL_REFLECTOR, Target.ID, CastInfos.Caster.ID));
            Target = CastInfos.Caster;
        }
        Fighter Caster = CastInfos.Caster;
        // Perd l'invisibilité s'il inflige des dommages direct
        if (!CastInfos.IsPoison && !CastInfos.IsTrap && !CastInfos.IsReflect) {
            Caster.states.removeState(FightStateEnum.Invisible);
        }

        // Application des buffs avant calcul totaux des dommages, et verification qu'ils n'entrainent pas la fin du combat
        if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
            if (Caster.buff.onAttackPostJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
            if (Target.buff.onAttackedPostJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        double num1 = 0.0;
        double num2 = (double) CastInfos.Caster.getLife() / (double) CastInfos.Caster.getMaxLife();
        if (num2 <= 0.5) {
            num1 = 2.0 * num2;
        } else if (num2 > 0.5) {
            num1 = 1.0 + (num2 - 0.5) * -2.0;
        }
        DamageJet.setValue((double) CastInfos.Caster.getLife() * num1 * (double) CastInfos.RandomJet(Target) / 100.0);

        // Calcul resistances
        Target.calculReduceDamages(Damage_Neutral, DamageJet);
        // Reduction des dommages grace a l'armure
        if (DamageJet.intValue() > 0) {
            // Si ce n'est pas des dommages direct on ne reduit pas
            if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
                // Calcul de l'armure par rapport a l'effet
                int Armor = Target.calculArmor(Damage_Neutral);
                // Si il reduit un minimum
                if (Armor != 0) {
                    // XX Reduit les dommages de X

                    Target.fight.sendToField(new GameActionFightReduceDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_MODERATOR, Target.ID, Target.ID, Armor));

                    // On reduit
                    DamageJet.setValue(DamageJet.intValue() - Armor);

                    // Si on suprimme totalement les dommages
                    if (DamageJet.intValue() < 0) {
                        DamageJet.setValue(0);
                    }
                }
            }
        }
        // Application des buffs apres le calcul totaux et l'armure
        if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
            if (Caster.buff.onAttackAfterJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
            if (Target.buff.onattackedafterjet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
        }

        // S'il subit des dommages
        if (DamageJet.getValue() > 0) {
            // Si c'est pas un poison ou un renvoi on applique le renvoie
            if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
                MutableInt ReflectDamage = new MutableInt(Target.getReflectedDamage());

                // Si du renvoi
                if (ReflectDamage.intValue() > 0 && Target.ID != Caster.ID) {
                    Target.fight.sendToField(new GameActionFightReflectDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_REFLECTOR, Target.ID, Caster.ID));

                    // Trop de renvois
                    if (ReflectDamage.getValue() > DamageJet.getValue()) {
                        ReflectDamage.setValue(DamageJet.getValue());
                    }

                    EffectCast SubInfos = new EffectCast(StatsEnum.DamageBrut, 0, (short) 0, 0, null, Target, null, false, StatsEnum.NONE, 0, null);
                    SubInfos.IsReflect = true;

                    // Si le renvoi de dommage entraine la fin de combat on stop
                    if (EffectDamage.ApplyDamages(SubInfos, Caster, ReflectDamage) == -3) {
                        return -3;
                    }

                    // Dommage renvoyé
                    DamageJet.add(-ReflectDamage.intValue());
                }
            }
        }
        // Peu pas etre en dessous de 0
        if (DamageJet.getValue() < 0) {
            DamageJet.setValue(0);
        }

        // Dommages superieur a la vie de la cible
        if (DamageJet.getValue() > Target.getLife()) {
            DamageJet.setValue(Target.getLife());
        }

        // Deduit la vie
        Target.setLife(Target.getLife() - DamageJet.intValue());

        // Enois du packet combat subit des dommages
        if (DamageJet.intValue() != 0) {
            Target.fight.sendToField(new GameActionFightLifePointsLostMessage(CastInfos.Effect != null ? CastInfos.Effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, Caster.ID, Target.ID, DamageJet.intValue(), 0));
        }
        return Target.tryDie(Caster.ID);
    }

}
