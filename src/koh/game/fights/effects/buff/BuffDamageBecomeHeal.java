package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectHeal;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffDamageBecomeHeal extends BuffEffect {

    public BuffDamageBecomeHeal(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        if (DamageInfos.IsReflect || DamageInfos.IsReturnedDamages || DamageInfos.IsPoison) {
            return -1;
        }
        // mort
        if (Caster.Dead()) {
            //Target.Buffs.RemoveBuff(this);
            return -1;
        }
        
        DamageValue.setValue(DamageValue.getValue() * this.CastInfos.Effect.value);

        if (EffectHeal.ApplyHeal(CastInfos, Target, DamageValue, false) == -3) {
            return -3;
        }
        DamageValue.setValue(0);
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
