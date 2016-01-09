package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPoutch;

/**
 *
 * @author Neo-Craft
 */
public class EffectPoutch extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {

        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffPoutch(castInfos, Target));
        }

        return -1;
    }

}
