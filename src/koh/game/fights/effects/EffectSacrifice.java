package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSacrifice;

/**
 *
 * @author Neo-Craft
 */
public class EffectSacrifice extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter target : CastInfos.Targets) {
            if (target.getTeam() != CastInfos.caster.getTeam() || target == CastInfos.caster) {
                continue;
            }

            target.getBuff().addBuff(new BuffSacrifice(CastInfos, target));
        }

        return -1;
    }

}
