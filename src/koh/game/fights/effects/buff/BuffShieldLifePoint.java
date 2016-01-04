package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 1/3/16.
 */
public class BuffShieldLifePoint extends BuffEffect {

    private int value1, jet;

    public BuffShieldLifePoint(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);

    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {

        this.jet = castInfos.randomJet(target);
        this.value1 = (jet * this.target.getLife()) / 100;
        this.target.setShieldPoints(target.getShieldPoints() + value1);

        this.target.getStats().addBoost(this.castInfos.EffectType, this.value1);

        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public int removeEffect() {

        this.target.getStats().getEffect(this.castInfos.EffectType).additionnal -= this.value1;

        this.target.setShieldPoints(target.getShieldPoints() < value1 ? 0 : target.getShieldPoints() - value1);

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.SpellId, this.castInfos.GetEffectUID(), this.castInfos.ParentUID, (short) Math.abs(this.jet));
    }
}

