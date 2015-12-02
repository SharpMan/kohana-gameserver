package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamagePerPA;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamagePerPA extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.buff.addBuff(new BuffDamagePerPA(CastInfos, Target));
        }

        return -1;
    }

}
