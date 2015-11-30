package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffAddResistStats extends BuffEffect {

    public int Value1;

    public BuffAddResistStats(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);

    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.Value1 = CastInfos.RandomJet(Target);

        this.Target.Stats.addBoost(StatsEnum.WaterElementResistPercent, this.Value1);
        this.Target.Stats.addBoost(StatsEnum.FireElementResistPercent, this.Value1);
        this.Target.Stats.addBoost(StatsEnum.AirElementResistPercent, this.Value1);
        this.Target.Stats.addBoost(StatsEnum.EarthElementResistPercent, this.Value1);
        this.Target.Stats.addBoost(StatsEnum.NeutralElementResistPercent, this.Value1);
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int RemoveEffect() {
        this.Target.Stats.getEffect(StatsEnum.WaterElementResistPercent).additionnal -= this.Value1;
        this.Target.Stats.getEffect(StatsEnum.FireElementResistPercent).additionnal -= this.Value1;
        this.Target.Stats.getEffect(StatsEnum.AirElementResistPercent).additionnal -= this.Value1;
        this.Target.Stats.getEffect(StatsEnum.EarthElementResistPercent).additionnal -= this.Value1;
        this.Target.Stats.getEffect(StatsEnum.NeutralElementResistPercent).additionnal -= this.Value1;

        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, this.IsDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.Value1));
    }
}
