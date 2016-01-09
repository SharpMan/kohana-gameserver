package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffAddResistStats;
import koh.game.fights.fighters.IllusionFighter;

/**
 *
 * @author Neo-Craft
 */
public class EffectAddAllResist extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            if (Target instanceof IllusionFighter) {
                continue;//Roulette tue clone ...
            }
            EffectCast SubInfos = new EffectCast(castInfos.effectType, castInfos.spellId, castInfos.cellId, castInfos.chance, castInfos.effect, castInfos.caster, castInfos.targets, castInfos.spellLevel);
            BuffAddResistStats BuffStats = new BuffAddResistStats(SubInfos, Target);
            if (Target.getBuff().buffMaxStackReached(BuffStats)) {
                return -1;
            }
            if (BuffStats.applyEffect(null, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(BuffStats);
        }

        return -1;
    }

}
