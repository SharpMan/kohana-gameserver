package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamagePerPA;

/**
 *
 * @author Neo-Craft
 */
public class EffectDamagePerPA extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffDamagePerPA(castInfos, Target));
        }

        return -1;
    }

}
