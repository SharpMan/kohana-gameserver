package koh.game.fights.effects;

import java.util.HashMap;

import koh.game.fights.Fighter;
import koh.game.fights.effects.buff.BuffStats;
import koh.protocol.client.enums.StatsEnum;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class EffectStatsSteal extends EffectBase {

    public static final HashMap<StatsEnum, StatsEnum> TARGET_MALUS = new HashMap<StatsEnum, StatsEnum>() {
        {
            this.put(StatsEnum.STEAL_VITALITY, StatsEnum.SUB_VITALITY);
            this.put(StatsEnum.STEAL_STRENGTH, StatsEnum.SUB_STRENGTH);
            this.put(StatsEnum.STEAL_INTELLIGENCE, StatsEnum.SUB_INTELLIGENCE);
            this.put(StatsEnum.STEAL_AGILITY, StatsEnum.SUB_AGILITY);
            this.put(StatsEnum.STEAL_WISDOM, StatsEnum.SUB_WISDOM);
            this.put(StatsEnum.STEAL_CHANCE, StatsEnum.SUB_CHANCE);
            this.put(StatsEnum.STEAL_PA, StatsEnum.SUB_PA);
            this.put(StatsEnum.STEAL_PM, StatsEnum.SUB_PM);
            this.put(StatsEnum.STEAL_RANGE, StatsEnum.SUB_RANGE);
        }
    };

    public static final HashMap<StatsEnum, StatsEnum> CASTER_BONUS = new HashMap<StatsEnum, StatsEnum>() {
        {
            this.put(StatsEnum.STEAL_VITALITY, StatsEnum.VITALITY);
            this.put(StatsEnum.STEAL_STRENGTH, StatsEnum.STRENGTH);
            this.put(StatsEnum.STEAL_INTELLIGENCE, StatsEnum.INTELLIGENCE);
            this.put(StatsEnum.STEAL_AGILITY, StatsEnum.AGILITY);
            this.put(StatsEnum.STEAL_WISDOM, StatsEnum.WISDOM);
            this.put(StatsEnum.STEAL_CHANCE, StatsEnum.CHANCE);
            this.put(StatsEnum.STEAL_PA, StatsEnum.ACTION_POINTS);
            this.put(StatsEnum.STEAL_PM, StatsEnum.MOVEMENT_POINTS);
            this.put(StatsEnum.STEAL_RANGE, StatsEnum.ADD_RANGE);
        }
    };

    @Override
    public int applyEffect(EffectCast CastInfos) {
        StatsEnum MalusType = TARGET_MALUS.get(CastInfos.effectType);
        StatsEnum BonusType = CASTER_BONUS.get(CastInfos.effectType);

        EffectCast MalusInfos = new EffectCast(MalusType, CastInfos.spellId, CastInfos.cellId, CastInfos.chance, CastInfos.effect, CastInfos.caster, CastInfos.targets, false, StatsEnum.NONE, 0, CastInfos.spellLevel, CastInfos.duration, 0);
        EffectCast BonusInfos = new EffectCast(BonusType, CastInfos.spellId, CastInfos.cellId, CastInfos.chance, CastInfos.effect, CastInfos.caster, CastInfos.targets, false, StatsEnum.NONE, 0, CastInfos.spellLevel, CastInfos.duration - 1, 0);
        MutableInt DamageValue = new MutableInt();

        for (Fighter Target : CastInfos.targets) {
            if (Target == CastInfos.caster) {
                continue;
            }

            // Malus a la cible
            BuffStats BuffStats = new BuffStats(MalusInfos, Target);
            if (BuffStats.applyEffect(DamageValue, null) == -3) {
                return -3;
            }

            Target.getBuff().addBuff(BuffStats);

            // Bonus au lanceur
            BuffStats = new BuffStats(BonusInfos, CastInfos.caster);
            if (BuffStats.applyEffect(DamageValue, null) == -3) {
                return -3;
            }

            CastInfos.caster.getBuff().addBuff(BuffStats);
        }

        return -1;
    }

}
