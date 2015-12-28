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
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            CastInfos.caster.getBuff().addBuff(new BuffPorteur(CastInfos, Target));
            Target.getBuff().addBuff(new BuffPorter(CastInfos, Target));
        }

        return -1;
    }

}
