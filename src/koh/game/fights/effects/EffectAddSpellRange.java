package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffAddSpellRange;

/**
 *
 * @author Neo-Craft
 */
public class EffectAddSpellRange extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        BuffEffect buff = null;
        for (Fighter target : castInfos.targets) {
            buff = new BuffAddSpellRange(castInfos, target);
            if (!target.getBuff().buffMaxStackReached(buff)) {
                target.getBuff().addBuff(buff);
                if (buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }

        return -1;
    }

}
