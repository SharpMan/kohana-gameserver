package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffSubResistStats;
import koh.game.fights.fighters.IllusionFighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectSubAllResist extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (final Fighter target : castInfos.targets) {
            if (target instanceof IllusionFighter) {
                continue;//Roulette tue clone ...
            }
            final EffectCast subInfos = new EffectCast(castInfos.effectType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            final BuffSubResistStats buffStats = new BuffSubResistStats(subInfos, target);

            if (target.getBuff().buffMaxStackReached(buffStats)) {
                return -1;
            }

            if (buffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            target.getBuff().addBuff(buffStats);
        }

        return -1;
    }

}
