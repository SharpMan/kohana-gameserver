package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDistributesDamagesOccasioned extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        int Apply = -1;
        for (Fighter Target : CastInfos.Targets) {
            int DamageJet = (CastInfos.DamageValue * CastInfos.RandomJet(Target)) / 100;
            if (DamageJet < 0) {
                DamageJet = (0);
            }

            // Dommages superieur a la vie de la cible
            if (DamageJet > Target.getLife()) {
                DamageJet = (Target.getLife());
            }

            // Deduit la vie
            Target.setLife(Target.getLife() - DamageJet);

            // Enois du packet combat subit des dommages
            if (DamageJet != 0) {
                Target.fight.sendToField(new GameActionFightLifePointsLostMessage(CastInfos.Effect != null ? CastInfos.Effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, CastInfos.Caster.ID, Target.ID, DamageJet, 0));
            }
            int newValue = Target.tryDie(CastInfos.Caster.ID);
            if (newValue < Apply) {
                Apply = newValue;
            }
        }
        return Apply;
    }

}
