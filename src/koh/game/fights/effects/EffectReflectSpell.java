package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffReflectSpell;

/**
 *
 * @author Neo-Craft
 */
public class EffectReflectSpell extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        if (castInfos.duration > 0) {
            for (Fighter Target : castInfos.targets) {
                Target.getBuff().addBuff(new BuffReflectSpell(castInfos, Target));
            }
        }

        return -1;
    }

}
