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
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.Value1 = CastInfos.randomJet(target);

        this.target.getStats().addBoost(StatsEnum.WaterElementResistPercent, this.Value1);
        this.target.getStats().addBoost(StatsEnum.FireElementResistPercent, this.Value1);
        this.target.getStats().addBoost(StatsEnum.AirElementResistPercent, this.Value1);
        this.target.getStats().addBoost(StatsEnum.EarthElementResistPercent, this.Value1);
        this.target.getStats().addBoost(StatsEnum.NeutralElementResistPercent, this.Value1);
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.target.getStats().getEffect(StatsEnum.WaterElementResistPercent).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.FireElementResistPercent).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.AirElementResistPercent).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.EarthElementResistPercent).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.NeutralElementResistPercent).additionnal -= this.Value1;

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.Value1));
    }
}
