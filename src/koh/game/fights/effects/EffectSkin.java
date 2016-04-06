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
    public int applyEffect(EffectCast castInfos) {
        BuffEffect buff = null;
        for (Fighter Target : castInfos.targets) {
            buff = new BuffSkin(castInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(buff)) {
                Target.getBuff().addBuff(buff);
                if (buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
