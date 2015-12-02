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
    public int ApplyEffect(EffectCast CastInfos) {
        BuffEffect Buf = null;
        for (Fighter Target : CastInfos.Targets) {
            Buf = new BuffDamageBecomeHeal(CastInfos, Target);
            if (!Target.buff.buffMaxStackReached(Buf)) {
                Target.buff.addBuff(Buf);
            }
        }
        return -1;
    }

}
