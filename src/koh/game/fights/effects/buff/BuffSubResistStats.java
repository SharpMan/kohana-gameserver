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

    public int value1;

    public BuffSubResistStats(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);

    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.value1 = castInfos.randomJet(target);

        this.target.getStats().addBoost(StatsEnum.SUB_EARTH_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.SUB_WATER_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.SUB_FIRE_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.SUB_NEUTRAL_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.SUB_AIR_RESIST_PERCENT, this.value1);
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.target.getStats().addBoost(StatsEnum.EARTH_ELEMENT_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.WATER_ELEMENT_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.FIRE_ELEMENT_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.NEUTRAL_ELEMENT_RESIST_PERCENT, this.value1);
        this.target.getStats().addBoost(StatsEnum.AIR_ELEMENT_RESIST_PERCENT, this.value1);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.value1));
    }
}
