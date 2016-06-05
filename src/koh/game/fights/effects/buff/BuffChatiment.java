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
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        if(damageInfos != null && damageInfos.isPoison){
            return super.applyEffect(damageValue, damageInfos);
        }
        final MutableInt buffValue = new MutableInt(damageValue.getValue() / 2); // Divise par deux les stats a boost car c'est un personnage.
        //var StatsType = (EffectEnum)this.castInfos.value1 == EffectEnum.HEAL ? EffectEnum.AddVitalite : (EffectEnum)this.castInfos.value1;
        int MaxValue = this.castInfos.effect.diceSide;
        int Duration = this.castInfos.effect.value;

        if (this.target.getFight().getCurrentFighter().getID() == this.castInfos.fakeValue) {
            if (this.castInfos.damageValue < MaxValue) {
                if (this.castInfos.damageValue + buffValue.getValue() > MaxValue) {
                    buffValue.setValue(MaxValue - this.castInfos.damageValue);
                }
            } else {
                buffValue.setValue(0);
            }
        } else {
            this.castInfos.damageValue = 0;
            this.castInfos.fakeValue = (int) this.target.getFight().getCurrentFighter().getID();

            if (this.castInfos.damageValue + buffValue.getValue() > MaxValue) {
                buffValue.setValue(MaxValue);
            }
        }
        if (buffValue.getValue() > 0) {
            this.castInfos.damageValue += buffValue.getValue();
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.valueOf(this.castInfos.effect.diceNum), this.castInfos.spellId, this.castInfos.cellId, 0, null, castInfos.caster, null, false, this.castInfos.effectType, buffValue.getValue(), null, Duration, this.getId()), this.target);

            BuffStats.applyEffect(buffValue, damageInfos);
            this.target.getBuff().addBuff(BuffStats);
        }

        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
