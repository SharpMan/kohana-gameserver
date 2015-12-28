package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffHeal;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsGainMessage;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class EffectHeal extends EffectBase {


    private static final Logger logger = LogManager.getLogger(EffectHeal.class);

    public static int ApplyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal) {
        return ApplyHeal(CastInfos, Target, Heal, true);
    }

    public static int ApplyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal, boolean Calculate) {
        Fighter Caster = CastInfos.caster;

        // boost soin etc
        if (Calculate) {
            Caster.calculheal(Heal);
        }
        
        if (Target.getBuff().onHealPostJet(CastInfos, Heal) == -3) {
            return -3; // Fin du combat
        }

        // Si le soin est superieur a sa vie actuelle
        if (Target.getLife() + Heal.getValue() > Target.getMaxLife()) {
            Heal.setValue(Target.getMaxLife() - Target.getLife());
            if (Heal.getValue() < 0) {
                logger.error("TargetMaxlife {} TargettLife {}" ,Target.getMaxLife(), Target.getLife());
            }
        }

        // Affectation
        Target.setLife(Target.getLife() + Heal.getValue());

        // Envoi du packet
        Target.getFight().sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, Caster.getID(), Target.getID(), Math.abs(Heal.getValue())));

        // Le soin entraine la fin du combat ?
        return Target.tryDie(Caster.getID());
    }

    @Override
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.Duration > 0) {
            // L'effet est un poison
            CastInfos.IsPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.Targets) {
                Target.getBuff().addBuff(new BuffHeal(CastInfos, Target));
            }
        } else // Heal direct
        {
            for (Fighter Target : CastInfos.Targets) {
                if (EffectHeal.ApplyHeal(CastInfos, Target, new MutableInt(CastInfos.randomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
