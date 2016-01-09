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
        BuffEffect Buf = null;
        for (Fighter Target : castInfos.targets) {
            Buf = new BuffArmor(castInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(Buf)) {
                Target.getBuff().addBuff(Buf);
            }
        }
        return -1;
    }

}
