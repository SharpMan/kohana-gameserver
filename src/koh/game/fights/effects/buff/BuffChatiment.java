package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffChatiment extends BuffEffect {

    public BuffChatiment(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt BuffValue = new MutableInt(DamageValue.getValue() / 2); // Divise par deux les stats a boost car c'est un personnage.
        //var StatsType = (EffectEnum)this.CastInfos.value1 == EffectEnum.Heal ? EffectEnum.AddVitalite : (EffectEnum)this.CastInfos.value1;
        int MaxValue = this.CastInfos.effect.diceSide;
        int Duration = this.CastInfos.effect.value;

        if (this.target.getFight().getCurrentFighter().getID() == this.CastInfos.FakeValue) {
            if (this.CastInfos.DamageValue < MaxValue) {
                if (this.CastInfos.DamageValue + BuffValue.getValue() > MaxValue) {
                    BuffValue.setValue(MaxValue - this.CastInfos.DamageValue);
                }
            } else {
                BuffValue.setValue(0);
            }
        } else {
            this.CastInfos.DamageValue = 0;
            this.CastInfos.FakeValue = (int) this.target.getFight().getCurrentFighter().getID();

            if (this.CastInfos.DamageValue + BuffValue.getValue() > MaxValue) {
                BuffValue.setValue(MaxValue);
            }
        }
        if (BuffValue.getValue() > 0) {
            this.CastInfos.DamageValue += BuffValue.getValue();
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.valueOf(this.CastInfos.effect.diceNum), this.CastInfos.SpellId, this.CastInfos.CellId, 0, null, CastInfos.caster, null, false, this.CastInfos.EffectType, BuffValue.getValue(), null, Duration, this.GetId()), this.target);

            BuffStats.applyEffect(BuffValue, DamageInfos);
            this.target.getBuff().addBuff(BuffStats);
        }

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.effect.effectUid, 0, (short) this.CastInfos.effect.diceNum, (short) this.CastInfos.effect.diceSide, (short) this.CastInfos.effect.value, (short) 0/*(this.CastInfos.effect.delay)*/);
    }

}
