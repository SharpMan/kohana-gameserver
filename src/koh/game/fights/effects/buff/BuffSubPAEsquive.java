package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDodgePointLossMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightPointsVariationMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSubPAEsquive extends BuffEffect {

    public BuffSubPAEsquive(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt LostAP = new MutableInt(CastInfos.randomJet(target));
        LostAP.setValue(LostAP.getValue() > target.getAP() ? target.getAP() : LostAP.getValue());
        CastInfos.DamageValue = target.calculDodgeAPMP(CastInfos.caster, LostAP.intValue(), false,CastInfos.Duration > 0);

        if (CastInfos.DamageValue != LostAP.intValue()) {
            target.getFight().sendToField(new GameActionFightDodgePointLossMessage(ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PA, caster.getID(), target.getID(), LostAP.getValue() - CastInfos.DamageValue));
        }

        if (CastInfos.DamageValue > 0) {
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.Sub_PA, this.CastInfos.SpellId, (short) this.CastInfos.SpellId, 0, null, this.CastInfos.caster, null, false, StatsEnum.NOT_DISPELLABLE, CastInfos.DamageValue, CastInfos.SpellLevel, duration, 0), this.target);
            BuffStats.applyEffect(LostAP, null);
            this.target.getBuff().addBuff(BuffStats);
            if (target.getID() == target.getFight().getCurrentFighter().getID()) {
                target.getFight().sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, this.caster.getID(), target.getID(), (short) CastInfos.DamageValue));
            }
        }

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.effect.effectUid, 0, (short) this.CastInfos.effect.diceNum, (short) this.CastInfos.effect.diceSide, (short) this.CastInfos.effect.value, (short) 0/*(this.CastInfos.effect.delay)*/);
    }

}
