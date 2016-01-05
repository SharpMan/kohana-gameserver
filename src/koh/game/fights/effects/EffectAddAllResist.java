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
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            if (Target instanceof IllusionFighter) {
                continue;//Roulette tue clone ...
            }
            EffectCast SubInfos = new EffectCast(CastInfos.effectType, CastInfos.spellId, CastInfos.cellId, CastInfos.chance, CastInfos.effect, CastInfos.caster, CastInfos.targets, CastInfos.spellLevel);
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
