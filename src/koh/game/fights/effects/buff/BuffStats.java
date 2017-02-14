package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightPointsVariationMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffStats extends BuffEffect {

    public int value1;

    public BuffStats(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);

    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        this.value1 = castInfos.randomJet(target);

        this.target.getStats().addBoost(this.castInfos.effectType, this.value1);

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.target.getStats().addBoost(this.castInfos.effectType, -this.value1);
        if(this.castInfos.effectType == StatsEnum.SUB_PM && target == castInfos.getFight().getCurrentFighter()){
            target.send(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_WIN, target.getID(), target.getID(), (short) this.value1));
        }

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) Math.max(0, (castInfos.spellId == 111 ? (duration -1) : this.duration)), this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.value1));
    }

}
