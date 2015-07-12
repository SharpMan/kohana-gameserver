package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffChatiment;

/**
 *
 * @author Neo-Craft
 */
public class EffectChatiment extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            Target.Buffs.AddBuff(new BuffChatiment(CastInfos, Target));
        }

        return -1;
    }

}
