package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDammageOcassioned;

/**
 *
 * @author Neo-Craft
 */
public class EffectDammageOcassioned extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffDammageOcassioned(castInfos, Target));
        }
        return -1;
    }

}
