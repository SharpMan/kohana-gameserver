package koh.game.fights.effects.buff;

import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.effects.buff.BuffDecrementType;
import koh.game.fights.effects.buff.BuffEffect;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 5/22/16.
 */
public class BuffIncreaseFinalDamage extends BuffEffect {

    private final int JET;

    public BuffIncreaseFinalDamage(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_BEGINTURN);
        this.JET = CastInfos.randomJet(Target);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast damageInfos) {
        if (EffectHelper.verifyEffectTrigger(damageInfos.caster, target, this.castInfos.spellLevel.getEffects(), damageInfos.effect, damageInfos.isCAC, this.castInfos.effect.triggers, damageInfos.cellId))
            DamageValue.add((DamageValue.intValue() * this.JET) / 100);
        return super.applyEffect(DamageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}
