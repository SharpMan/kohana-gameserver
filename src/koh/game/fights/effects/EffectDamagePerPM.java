package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamagePerPM;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamagePerPM extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            Target.getBuff().addBuff(new BuffDamagePerPM(CastInfos, Target));
        }

        return -1;
    }

}