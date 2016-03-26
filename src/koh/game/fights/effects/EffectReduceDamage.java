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
    public int applyEffect(EffectCast castInfos) {
        BuffEffect buf;
        for (Fighter Target : castInfos.targets) {
            buf = new BuffReduceDamage(castInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(buf)) {
                Target.getBuff().addBuff(buf);
            }
        }
        return -1;
    }

}