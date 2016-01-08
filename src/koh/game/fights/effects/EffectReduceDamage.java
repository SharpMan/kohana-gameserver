package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffReduceDamage;

/**
 *
 * @author Melancholia
 */
public class EffectReduceDamage extends EffectBase {


    @Override
    public int applyEffect(EffectCast CastInfos) {
        BuffEffect buf = null;
        for (Fighter Target : CastInfos.targets) {
            buf = new BuffReduceDamage(CastInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(buf)) {
                Target.getBuff().addBuff(buf);
            }
        }
        return -1;
    }

}