package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffArmor;
import koh.game.fights.effects.buff.BuffEffect;

/**
 *
 * @author Neo-Craft
 */
public class EffectArmor extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        BuffEffect buf;
        for (Fighter Target : castInfos.targets) {
            buf = new BuffArmor(castInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(buf)) {
                buf.applyEffect(null,null);
                Target.getBuff().addBuff(buf);
            }
        }
        return -1;
    }

}
