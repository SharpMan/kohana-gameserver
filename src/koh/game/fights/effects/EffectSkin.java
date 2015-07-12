package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSkin;

/**
 *
 * @author Neo-Craft
 */
public class EffectSkin extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buff = null;
        for (Fighter Target : CastInfos.Targets) {
            Buff = new BuffSkin(CastInfos, Target);
            if (!Target.Buffs.BuffMaxStackReached(Buff)) {
                Target.Buffs.AddBuff(Buff);
                if (Buff.ApplyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
