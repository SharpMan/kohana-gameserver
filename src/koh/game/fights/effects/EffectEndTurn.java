package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEndTurn;

/**
 *
 * @author Melancholia
 */
public class EffectEndTurn extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.buff.addBuff(new BuffEndTurn(CastInfos, Target));
        }

        return -1;
    }

}