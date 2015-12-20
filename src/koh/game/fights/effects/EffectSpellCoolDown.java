package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSpellCoolDown;

/**
 *
 * @author Neo-Craft
 */
public class EffectSpellCoolDown extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {

        for (Fighter Target : CastInfos.Targets) {
            BuffEffect Buff = new BuffSpellCoolDown(CastInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(Buff)) {
                Target.getBuff().addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

}
