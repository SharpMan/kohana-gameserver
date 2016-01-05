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

    public static int applyHeal(EffectCast CastInfos, Fighter Target, MutableInt Heal) {
        return applyHeal(CastInfos, Target, Heal, true);
    }

    public static int applyHeal(EffectCast castInfos, Fighter target, MutableInt heal, boolean calculate) {
        Fighter Caster = castInfos.caster;

        // boost soin etc
        if (calculate) {
            Caster.calculheal(heal);
        }
        
        if (target.getBuff().onHealPostJet(castInfos, heal) == -3) {
            return -3; // Fin du combat
        }

        // Si le soin est superieur a sa vie actuelle
        if (target.getLife() + heal.getValue() > target.getMaxLife()) {
            heal.setValue(target.getMaxLife() - target.getLife());
            if (heal.getValue() < 0) {
                logger.error("TargetMaxlife {} TargettLife {}" ,target.getMaxLife(), target.getLife());
            }
        }

        // Affectation
        target.setLife(target.getLife() + heal.getValue());

        // Envoi du packet
        if(heal.getValue() != 0)
            target.getFight().sendToField(new GameActionFightLifePointsGainMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_POINTS_LOST, Caster.getID(), target.getID(), Math.abs(heal.getValue())));

        // Le soin entraine la fin du combat ?
        return target.tryDie(Caster.getID());
    }

    @Override
    public int applyEffect(EffectCast CastInfos) {
        // Si > 0 alors c'est un buff
        if (CastInfos.duration > 0) {
            // L'effet est un poison
            CastInfos.isPoison = true;

            // Ajout du buff
            for (Fighter Target : CastInfos.targets) {
                Target.getBuff().addBuff(new BuffHeal(CastInfos, Target));
            }
        } else // HEAL direct
        {
            for (Fighter Target : CastInfos.targets) {
                if (EffectHeal.applyHeal(CastInfos, Target, new MutableInt(CastInfos.randomJet(Target))) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
