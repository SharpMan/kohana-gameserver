package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEndTurn;

/**
 *
 * @author Melancholia
 */
public class EffectEndTurn extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            Target.getBuff().addBuff(new BuffEndTurn(CastInfos, Target));
        }

        return -1;
    }

}