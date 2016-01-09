package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffChatiment;

/**
 *
 * @author Neo-Craft
 */
public class EffectChatiment extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            Target.getBuff().addBuff(new BuffChatiment(castInfos, Target));
        }

        return -1;
    }

}
