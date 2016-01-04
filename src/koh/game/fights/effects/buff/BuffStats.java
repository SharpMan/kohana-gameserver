package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
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

        this.target.getStats().addBoost(this.castInfos.EffectType, this.value1);

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        
        this.target.getStats().getEffect(this.castInfos.EffectType).additionnal -= this.value1;

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.SpellId, this.castInfos.GetEffectUID(), this.castInfos.ParentUID, (short) Math.abs(this.value1));
    }

}
