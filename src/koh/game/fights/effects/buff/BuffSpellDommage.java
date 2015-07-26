package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import koh.protocol.types.game.actions.fight.FightTemporarySpellBoostEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;

/**
 *
 * @author Neo-Craft
 */
public class BuffSpellDommage extends BuffEffect {

    public BuffSpellDommage(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporarySpellBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, this.IsDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.diceNum);
    }

}
