package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSacrifice;

/**
 *
 * @author Neo-Craft
 */
public class EffectSacrifice extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            if (target.getTeam() != castInfos.caster.getTeam() || target == castInfos.caster) {
                continue;
            }

            target.getBuff().addBuff(new BuffSacrifice(castInfos, target));
        }

        return -1;
    }

}
