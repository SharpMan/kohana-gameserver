package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifeAndShieldPointsLostMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDistributesDamagesOccasioned extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        int apply = -1;
        for (Fighter target : castInfos.targets) {
            int damageJet = (castInfos.damageValue * castInfos.randomJet(target)) / 100;
            if (damageJet < 0 || (castInfos.caster.hasState(FightStateEnum.PACIFISTE.value) && !castInfos.isGlyph)) {
                damageJet = (0);
            }


            // Dommages superieur a la vie de la cible
            // Dommages superieur a la vie de la cible
            if (damageJet  > target.getLife() + target.getShieldPoints()) {
                damageJet = (target.getLife() + target.getShieldPoints());
            }

            // On verifie les point bouclier d'abord
            if(target.getShieldPoints() > 0){
                if(target.getShieldPoints() > damageJet){
                    target.setShieldPoints(target.getShieldPoints() - damageJet);
                    target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST,  castInfos.caster.getID(), target.getID(), 0, 0, damageJet));
                }
                else{
                    int lifePointRemaining = damageJet - target.getShieldPoints();
                    target.getFight().sendToField(new GameActionFightLifeAndShieldPointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, castInfos.caster.getID(), target.getID(), lifePointRemaining, 0, target.getShieldPoints()));
                    target.setLife(target.getLife() - lifePointRemaining);
                    target.setShieldPoints(0);
                }
            }
            else {
                // Deduit la vie
                target.setLife(target.getLife() - damageJet);

                // Enois du packet combat subit des dommages
                if (damageJet != 0) {
                    target.getFight().sendToField(new GameActionFightLifePointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, castInfos.caster.getID(), target.getID(), damageJet, 0));
                }
            }
            int newValue = target.tryDie(castInfos.caster.getID());
            if (newValue < apply) {
                apply = newValue;
            }
        }
        return apply;
    }

}
