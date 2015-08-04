package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffErosionPercent;

/**
 *
 * @author Neo-Craft
 */
public class EffectErosionPercent extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buf;
        for (Fighter Target : CastInfos.Targets) {
            Buf = new BuffErosionPercent(CastInfos, Target);
            if (!Target.Buffs.BuffMaxStackReached(Buf)) {
                if (Buf.ApplyEffect(null, null) == -3) {
                    return -3;
                } else {
                    Target.Buffs.AddBuff(Buf);
                }
            }
        }

        return -1;
    }

}
