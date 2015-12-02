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
            if (Target.team != CastInfos.Caster.team || Target == CastInfos.Caster) {
                continue;
            }

            Target.buff.addBuff(new BuffSacrifice(CastInfos, Target));
        }

        return -1;
    }

}
