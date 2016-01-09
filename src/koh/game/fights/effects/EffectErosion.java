package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffErosion;

/**
 *
 * @author Neo-Craft
 */
public class EffectErosion extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffErosion(castInfos, Target));
        }

        return -1;
    }

}