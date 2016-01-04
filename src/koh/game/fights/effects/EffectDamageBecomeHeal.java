package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffDamageBecomeHeal;
import koh.game.fights.effects.buff.BuffEffect;

/**
 *
 * @author Melancholia
 */
public class EffectDamageBecomeHeal extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        BuffEffect Buf = null;
        for (Fighter Target : CastInfos.targets) {
            Buf = new BuffDamageBecomeHeal(CastInfos, Target);
            if (!Target.getBuff().buffMaxStackReached(Buf)) {
                Target.getBuff().addBuff(Buf);
            }
        }
        return -1;
    }

}
