package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightLifePointsLostMessage;

/**
 * Created by Melancholia on 1/28/16.
 */
public class EffectSubLifePercent extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target:
             castInfos.targets) {
            final int value = (int)(target.getLife() * castInfos.randomJet(target) / 100f);
            target.setLife(value);
            target.getFight().sendToField(new GameActionFightLifePointsLostMessage(castInfos.effect != null ? castInfos.effect.effectId : ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, castInfos.caster.getID(), target.getID(), value, 0));

        }
        return -1;
    }
}
