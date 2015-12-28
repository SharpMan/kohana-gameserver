package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffReflectSpell;

/**
 *
 * @author Neo-Craft
 */
public class EffectReflectSpell extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        if (CastInfos.Duration > 0) {
            for (Fighter Target : CastInfos.Targets) {
                Target.getBuff().addBuff(new BuffReflectSpell(CastInfos, Target));
            }
        }

        return -1;
    }

}
