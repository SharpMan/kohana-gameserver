package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamage;
import koh.game.fights.effects.buff.BuffReflectSpell;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReduceDamagesMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReflectDamagesMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightReflectSpellMessage;
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
            CastInfos.Targets.stream().forEach((Target) -> {
                Target.getBuff().addBuff(new BuffDamage(CastInfos, Target));
            });
        } else // Dommage direct
        {
            for (Fighter Target : CastInfos.Targets) {
                //Eppe de iop ?
                MutableInt DamageValue = new MutableInt(CastInfos.randomJet(Target));

                if (EffectDamage.ApplyDamages(CastInfos, Target, DamageValue) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

    public static int ApplyDamages(EffectCast CastInfos, Fighter Target, MutableInt DamageJet) {

        if (Target.getStates().hasState(FightStateEnum.STATE_REFLECT_SPELL) && !CastInfos.IsPoison && ((BuffReflectSpell) Target.getStates().getBuffByState(FightStateEnum.STATE_REFLECT_SPELL)).reflectLevel >= CastInfos.SpellLevel.getGrade()) {
            Target.getFight().sendToField(new GameActionFightReflectSpellMessage(ActionIdEnum.ACTION_CHARACTER_SPELL_REFLECTOR, Target.getID(), CastInfos.caster.getID()));
            Target = CastInfos.caster;
        }
        Fighter Caster = CastInfos.caster;
        // Perd l'invisibilité s'il inflige des dommages direct
        if (!CastInfos.IsPoison && !CastInfos.IsTrap && !CastInfos.IsReflect) {
            Caster.getStates().removeState(FightStateEnum.Invisible);
        }

        // Application des buffs avant calcul totaux des dommages, et verification qu'ils n'entrainent pas la fin du combat
        if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
            if (Caster.getBuff().onAttackPostJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
            if (Target.getBuff().onAttackedPostJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        if(!CastInfos.IsReflect && CastInfos.IsTrap){
            if (Target.getBuff().onAttackedPostJetTrap(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
        }
        // Calcul jet
        Caster.computeDamages(CastInfos.EffectType, DamageJet);
        //Calcul Bonus Negatif Zone ect ...
        if (CastInfos.Effect != null) {
            Caster.calculBonusDamages(CastInfos.Effect, DamageJet,CastInfos.CellId , Target.getCellId(),CastInfos.oldCell);
        }

        // Calcul resistances
        Target.calculReduceDamages(CastInfos.EffectType, DamageJet);
        // Reduction des dommages grace a l'armure
        if (DamageJet.intValue() > 0) {
            // Si ce n'est pas des dommages direct on ne reduit pas
            if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
                // Calcul de l'armure par rapport a l'effet
                int Armor = Target.calculArmor(CastInfos.EffectType);
                // Si il reduit un minimum
                if (Armor != 0) {
                    // XX Reduit les dommages de X

                    Target.getFight().sendToField(new GameActionFightReduceDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_MODERATOR, Target.getID(), Target.getID(), Armor));

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
            if (Caster.getBuff().onAttackAfterJet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
            if (Target.getBuff().onattackedafterjet(CastInfos, DamageJet) == -3) {
                return -3; // Fin du combat
            }
        }

        // S'il subit des dommages
        if (DamageJet.getValue() > 0) {
            // Si c'est pas un poison ou un renvoi on applique le renvoie
            if (!CastInfos.IsPoison && !CastInfos.IsReflect) {
                MutableInt ReflectDamage = new MutableInt(Target.getReflectedDamage());

                // Si du renvoi
                if (ReflectDamage.intValue() > 0 && Target.getID() != Caster.getID()) {
                    Target.getFight().sendToField(new GameActionFightReflectDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_REFLECTOR, Target.getID(), Caster.getID()));

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
            Target.getFight().sendToField(new GameActionFightLifePointsLostMessage(CastInfos.Effect != null ? CastInfos.Effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, Caster.getID(), Target.getID(), DamageJet.intValue(), 0));
        }
        return Target.tryDie(Caster.getID());
    }

}
