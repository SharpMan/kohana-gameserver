package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostStateEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffState extends BuffEffect {

    public BuffState(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        this.Target.States.AddState(this);
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int RemoveEffect() {
        this.Target.States.DelState(this);

        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporaryBoostStateEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.DISPELLABLE_BY_STRONG_DISPEL, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.CastInfos.RandomJet(Target)), (short)CastInfos.Effect.value);
    }

}
