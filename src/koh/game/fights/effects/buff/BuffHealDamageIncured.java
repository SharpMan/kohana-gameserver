package koh.game.fights.effects.buff;

import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectHeal;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 1/29/16.
 */
public class BuffHealDamageIncured extends BuffEffect {

    private final int JET;

    public BuffHealDamageIncured(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.JET = CastInfos.randomJet(Target);
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        if (EffectHelper.verifyEffectTrigger(damageInfos.caster, target, this.castInfos.spellLevel.getEffects(), damageInfos.effect, damageInfos.isCAC, this.castInfos.effect.triggers, damageInfos.cellId)) {
            final MutableInt heal = new MutableInt((damageValue.intValue() * this.JET) / 100f);
            return EffectHeal.applyHeal(castInfos, damageInfos.caster, heal, false);
        }
        return super.applyEffect(damageValue,damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}