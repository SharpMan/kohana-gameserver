package koh.game.fights.effects;

import koh.game.fights.Fight.FightLoopState;
import koh.game.fights.Fighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectFinishTour extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        CastInfos.Caster.Fight.FightLoopState = FightLoopState.STATE_END_TURN;

        return -1;
    }

}
