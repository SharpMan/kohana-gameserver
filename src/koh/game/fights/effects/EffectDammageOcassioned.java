package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDammageOcassioned;

/**
 *
 * @author Neo-Craft
 */
public class EffectDammageOcassioned extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            Target.getBuff().addBuff(new BuffDammageOcassioned(CastInfos, Target));
        }
        return -1;
    }

}
