package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDodgePointLossMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSubPMEsquive extends BuffEffect {

    public BuffSubPMEsquive(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt LostPM = new MutableInt(castInfos.randomJet(target));
        LostPM.setValue(LostPM.getValue() > target.getAP() ? target.getAP() : LostPM.getValue());
        castInfos.damageValue = target.calculDodgeAPMP(castInfos.caster, LostPM.intValue(), true, castInfos.duration > 0);

        if (castInfos.damageValue != LostPM.intValue()) {
            target.getFight().sendToField(new GameActionFightDodgePointLossMessage(ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PM, caster.getID(), target.getID(), LostPM.getValue() - castInfos.damageValue));
        }

        if (castInfos.damageValue > 0) {
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.SUB_PM, this.castInfos.spellId, (short) this.castInfos.spellId, 0, null, this.castInfos.caster, null, false, StatsEnum.NOT_DISPELLABLE, castInfos.damageValue, castInfos.spellLevel, duration, 0), this.target);
            BuffStats.applyEffect(LostPM, null);
            this.target.getBuff().addBuff(BuffStats);
            if (target.getID() == target.getFight().getCurrentFighter().getID()) {
                // target.fight.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_MOVEMENT_POINTS_LOST, this.caster.getID(), target.getID(), (short) -castInfos.damageValue));
            }
        }

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
