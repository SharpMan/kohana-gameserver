package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectHeal;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffChatiment extends BuffEffect {

    final int maxValue, duration2;

    public BuffChatiment(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_BEGINTURN);
        this.duration--;
        maxValue = this.castInfos.effect.diceSide;
        duration2 = this.castInfos.effect.value;
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        if((damageInfos != null && damageInfos.isPoison) ||
                (target.getStates().hasState(FightStateEnum.INVULNERABLE) || target.getStates().hasState(FightStateEnum.Invulnérable))){
            return super.applyEffect(damageValue, damageInfos);
        }
        final MutableInt buffValue = new MutableInt(damageValue.getValue() / 2); // Divise par deux les stats a boost car c'est un personnage.
        //var StatsType = (EffectEnum)this.castInfos.value1 == EffectEnum.HEAL ? EffectEnum.AddVitalite : (EffectEnum)this.castInfos.value1;
        final int maxValue = this.castInfos.effect.diceSide;

        if (this.target.getFight().getCurrentFighter().getID() == this.castInfos.fakeValue) {
            if (this.castInfos.damageValue < maxValue) {
                if (this.castInfos.damageValue + buffValue.getValue() > maxValue) {
                    buffValue.setValue(maxValue - this.castInfos.damageValue);
                }
            } else {
                buffValue.setValue(0);
            }
        } else {
            this.castInfos.damageValue = 0;
            this.castInfos.fakeValue = (int) this.target.getFight().getCurrentFighter().getID();

            if (this.castInfos.damageValue + buffValue.getValue() > maxValue) {
                buffValue.setValue(maxValue);
            }
        }
        if (buffValue.getValue() > 0) {
            this.castInfos.damageValue += buffValue.getValue();
            switch (StatsEnum.valueOf(this.castInfos.effect.diceNum)){
                case HEAL:
                case PDV_REPORTS:
                    EffectHeal.applyHeal(castInfos,target,buffValue,false);
                    break;
                default:

                    final BuffStats buffStats = new BuffStats(new EffectCast(StatsEnum.valueOf(this.castInfos.effect.diceNum), this.castInfos.spellId, this.castInfos.cellId, 0, null, castInfos.caster, null, false, this.castInfos.effectType, buffValue.getValue(), null, duration2, this.getId()), this.target);

                    buffStats.applyEffect(buffValue, damageInfos);
                    this.target.getBuff().addBuff(buffStats);
                    break;
            }


        }

        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
