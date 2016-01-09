package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDodge;

/**
 *
 * @author Neo-Craft
 */
public class EffectDodge extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffDodge(castInfos, Target));
        }

        return -1;
    }

}
