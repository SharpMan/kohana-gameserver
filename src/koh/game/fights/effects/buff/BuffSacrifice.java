package koh.game.fights.effects.buff;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSacrifice extends BuffEffect {

    public BuffSacrifice(EffectCast CastInfos, Fighter Target) {
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

        DamageValue.setValue(0);

        DamageInfos.IsReturnedDamages = true;

        return EffectDamage.ApplyDamages(DamageInfos, CastInfos.Caster, new MutableInt(DamageInfos.RandomJet(Caster)));

    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.CastInfos.Effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.delay);
    }

}
