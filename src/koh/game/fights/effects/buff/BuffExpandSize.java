package koh.game.fights.effects.buff;

import java.util.ArrayList;
import java.util.List;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_CHANGE_LOOK;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightChangeLookMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffExpandSize extends BuffEffect {

    private final short OldScale;

    public BuffExpandSize(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.OldScale = Target.GetEntityLook().scales.get(0);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        this.Target.entityLook.scales.clear();
        this.Target.entityLook.scales.add((short) (OldScale + (((double) this.OldScale * CastInfos.Effect.diceNum) / 100)));
        this.Caster.Fight.sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.Caster.ID, this.Target.ID, this.Target.GetEntityLook()));
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int RemoveEffect() {
        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.DISPELLABLE_BY_DEATH, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) this.CastInfos.Effect.diceNum);
    }

}
