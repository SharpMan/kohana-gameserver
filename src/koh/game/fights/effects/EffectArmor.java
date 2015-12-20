package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffArmor;
import koh.game.fights.effects.buff.BuffEffect;

/**
 *
 * @author Neo-Craft
 */
public class EffectArmor extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buf = null;
        for (Fighter Target : CastInfos.Targets) {
            Buf = new BuffArmor(CastInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(Buf)) {
                Target.getBuff().addBuff(Buf);
            }
        }
        return -1;
    }

}
