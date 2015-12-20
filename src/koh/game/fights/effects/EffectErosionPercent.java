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
            if (!Target.getBuff().buffMaxStackReached(Buf)) {
                if (Buf.applyEffect(null, null) == -3) {
                    return -3;
                } else {
                    Target.getBuff().addBuff(Buf);
                }
            }
        }

        return -1;
    }

}
