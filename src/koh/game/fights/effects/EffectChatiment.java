package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffChatiment;

/**
 *
 * @author Neo-Craft
 */
public class EffectChatiment extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.getBuff().addBuff(new BuffChatiment(CastInfos, Target));
        }

        return -1;
    }

}
