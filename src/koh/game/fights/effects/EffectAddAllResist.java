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
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (Target instanceof IllusionFighter) {
                continue;//Roulette tue clone ...
            }
            EffectCast SubInfos = new EffectCast(CastInfos.EffectType, CastInfos.SpellId, CastInfos.CellId, CastInfos.Chance, CastInfos.Effect, CastInfos.Caster, CastInfos.Targets, CastInfos.SpellLevel);
            BuffAddResistStats BuffStats = new BuffAddResistStats(SubInfos, Target);
            if (Target.Buffs.BuffMaxStackReached(BuffStats)) {
                return -1;
            }
            if (BuffStats.ApplyEffect(null, null) == -3) {
                return -3;
            }

            Target.Buffs.AddBuff(BuffStats);
        }

        return -1;
    }

}
