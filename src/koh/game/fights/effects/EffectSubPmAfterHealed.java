package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPmAfterHealed;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPmAfterHealed extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffSubPmAfterHealed(castInfos, Target));
        }
        return -1;
    }

}
