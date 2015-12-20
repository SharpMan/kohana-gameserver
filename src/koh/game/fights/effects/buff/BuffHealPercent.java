package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectHealPercent;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffHealPercent extends BuffEffect {

    public BuffHealPercent(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_BEGINTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    /**
     *
     * @param DamageValue
     * @param DamageInfos
     * @return
     */
    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (EffectHealPercent.ApplyHealPercent(CastInfos, target, CastInfos.RandomJet(target)) == -3) {
            return -3;
        }
        return -1;
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.Duration, FightDispellableEnum.DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) Math.abs(CastInfos.RandomJet(target)));
    }

}
