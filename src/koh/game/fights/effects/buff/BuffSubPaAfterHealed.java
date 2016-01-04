package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSubPaAfterHealed extends BuffEffect {

    public BuffSubPaAfterHealed(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_HEAL_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt HealValue, EffectCast DamageInfos) {
        return EffectBase.getEffect(StatsEnum.SubPAEsquive).applyEffect(CastInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.duration, (byte) 0, this.CastInfos.SpellId, this.CastInfos.effect.effectUid, 0, (short) this.CastInfos.effect.diceNum, (short) this.CastInfos.effect.diceSide, (short) this.CastInfos.effect.value, (short) 0/*(this.CastInfos.effect.delay)*/);
    }

}
