package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffPorter;
import koh.game.fights.effects.buff.BuffPorteur;

/**
 *
 * @author Neo-Craft
 */
public class EffectPorter extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            CastInfos.Caster.Buffs.AddBuff(new BuffPorteur(CastInfos, Target));
            Target.Buffs.AddBuff(new BuffPorter(CastInfos, Target));
        }

        return -1;
    }

}
