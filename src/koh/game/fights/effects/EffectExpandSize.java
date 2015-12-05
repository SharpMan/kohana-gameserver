package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffExpandSize;

/**
 *
 * @author Neo-Craft
 */
public class EffectExpandSize extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buff = null;
        for (Fighter Target : CastInfos.Targets) {
            Buff = new BuffExpandSize(CastInfos, Target);
            if (!Target.buff.buffMaxStackReached(Buff)) {
                Target.buff.addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

}
