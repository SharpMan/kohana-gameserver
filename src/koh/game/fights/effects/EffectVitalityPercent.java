package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.StatsEnum;

/**
 *
 * @author Neo-Craft
 */
public class EffectVitalityPercent extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            EffectCast SubInfos = new EffectCast(StatsEnum.Vitality, CastInfos.SpellId, CastInfos.CellId, CastInfos.Chance, null, CastInfos.Caster, CastInfos.Targets, CastInfos.SpellLevel);
            SubInfos.DamageValue = (int) (((double) Target.getLife() / 100) * CastInfos.RandomJet(Target));
            SubInfos.Duration = CastInfos.Duration;
            BuffStats BuffStats = new BuffStats(SubInfos, Target);
            if (BuffStats.ApplyEffect(null, null) == -3) {
                return -3;
            }

            Target.buff.addBuff(BuffStats);
        }

        return -1;
    }

}
