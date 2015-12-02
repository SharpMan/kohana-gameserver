package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPmAfterHealed;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPmAfterHealed extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.buff.addBuff(new BuffSubPmAfterHealed(CastInfos, Target));
        }
        return -1;
    }

}
