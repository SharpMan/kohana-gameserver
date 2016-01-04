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
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.Value1 = castInfos.randomJet(target);

        this.target.getStats().addBoost(StatsEnum.SUB_EARTH_RESIST_PERCENT, this.Value1);
        this.target.getStats().addBoost(StatsEnum.SUB_WATER_ELEMENT_REDUCTION, this.Value1);
        this.target.getStats().addBoost(StatsEnum.SUB_FIRE_ELEMENT_REDUCTION, this.Value1);
        this.target.getStats().addBoost(StatsEnum.SUB_NEUTRAL_ELEMENT_REDUCTION, this.Value1);
        this.target.getStats().addBoost(StatsEnum.SUB_AIR_ELEMENT_REDUCTION, this.Value1);
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.target.getStats().getEffect(StatsEnum.SUB_EARTH_RESIST_PERCENT).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.SUB_WATER_ELEMENT_REDUCTION).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.SUB_FIRE_ELEMENT_REDUCTION).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.SUB_NEUTRAL_ELEMENT_REDUCTION).additionnal -= this.Value1;
        this.target.getStats().getEffect(StatsEnum.SUB_AIR_ELEMENT_REDUCTION).additionnal -= this.Value1;

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.SpellId, this.castInfos.GetEffectUID(), this.castInfos.ParentUID, (short) Math.abs(this.Value1));
    }
}
