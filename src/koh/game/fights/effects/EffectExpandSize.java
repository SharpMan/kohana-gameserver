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
    public int applyEffect(EffectCast castInfos) {
        BuffEffect Buff = null;
        for (Fighter Target : castInfos.targets) {
            Buff = new BuffExpandSize(castInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(Buff)) {
                Target.getBuff().addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

}
