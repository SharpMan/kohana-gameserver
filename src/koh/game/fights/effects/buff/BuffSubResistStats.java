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
public class BuffSubResistStats extends BuffEffect {

    public int Value1;

    public BuffSubResistStats(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);

    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.Value1 = CastInfos.RandomJet(Target);

        this.Target.Stats.AddBoost(StatsEnum.Sub_Earth_Resist_Percent, this.Value1);
        this.Target.Stats.AddBoost(StatsEnum.Sub_Water_Element_Reduction, this.Value1);
        this.Target.Stats.AddBoost(StatsEnum.Sub_Fire_Element_Reduction, this.Value1);
        this.Target.Stats.AddBoost(StatsEnum.Sub_Neutral_Element_Reduction, this.Value1);
        this.Target.Stats.AddBoost(StatsEnum.Sub_Air_Element_Reduction, this.Value1);
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int RemoveEffect() {
        this.Target.Stats.GetEffect(StatsEnum.Sub_Earth_Resist_Percent).additionnal -= this.Value1;
        this.Target.Stats.GetEffect(StatsEnum.Sub_Water_Element_Reduction).additionnal -= this.Value1;
        this.Target.Stats.GetEffect(StatsEnum.Sub_Fire_Element_Reduction).additionnal -= this.Value1;
        this.Target.Stats.GetEffect(StatsEnum.Sub_Neutral_Element_Reduction).additionnal -= this.Value1;
        this.Target.Stats.GetEffect(StatsEnum.Sub_Air_Element_Reduction).additionnal -= this.Value1;

        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, this.IsDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.Value1));
    }
}
