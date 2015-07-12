package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffErosion;

/**
 *
 * @author Neo-Craft
 */
public class EffectErosion extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.Buffs.AddBuff(new BuffErosion(CastInfos, Target));
        }

        return -1;
    }

}