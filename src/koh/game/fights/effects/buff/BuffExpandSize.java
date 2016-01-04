package koh.game.fights.effects.buff;

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
        this.OldScale = Target.getEntityLook().scales.get(0);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        this.target.getEntityLook().scales.clear();
        this.target.getEntityLook().scales.add((short) (OldScale + (((double) this.OldScale * castInfos.effect.diceNum) / 100)));
        this.caster.getFight().sendToField(new GameActionFightChangeLookMessage(ACTION_CHARACTER_CHANGE_LOOK, this.caster.getID(), this.target.getID(), this.target.getEntityLook()));
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE_BY_DEATH, (short) this.castInfos.SpellId, this.castInfos.GetEffectUID(), this.castInfos.ParentUID, (short) this.castInfos.effect.diceNum);
    }

}
