package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubPaAfterHealed;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubPaAfterHealed extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffSubPaAfterHealed(castInfos, Target));
        }
        return -1;
    }

}
