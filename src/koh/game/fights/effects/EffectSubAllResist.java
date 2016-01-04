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
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            if (Target instanceof IllusionFighter) {
                continue;//Roulette tue clone ...
            }
            EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, CastInfos.CellId, CastInfos.Chance, CastInfos.effect, CastInfos.caster, CastInfos.targets, CastInfos.SpellLevel);
            BuffSubResistStats BuffStats = new BuffSubResistStats(SubInfos, Target);

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
