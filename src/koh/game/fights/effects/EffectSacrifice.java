package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSacrifice;

/**
 *
 * @author Neo-Craft
 */
public class EffectSacrifice extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (Target.Team != CastInfos.Caster.Team || Target == CastInfos.Caster) {
                continue;
            }

            Target.Buffs.AddBuff(new BuffSacrifice(CastInfos, Target));
        }

        return -1;
    }

}
