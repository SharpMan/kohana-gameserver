package koh.game.fights.effects;

import koh.game.fights.Fight.FightLoopState;

/**
 *
 * @author Neo-Craft
 */
public class EffectFinishTour extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        CastInfos.caster.getFight().setFightLoopState(FightLoopState.STATE_END_TURN);

        return -1;
    }

}
