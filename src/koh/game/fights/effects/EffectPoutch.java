package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPoutch;

/**
 *
 * @author Neo-Craft
 */
public class EffectPoutch extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {

        for (Fighter Target : CastInfos.Targets) {
            Target.Buffs.AddBuff(new BuffPoutch(CastInfos, Target));
        }

        return -1;
    }

}
