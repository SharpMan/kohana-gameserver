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
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt LostAP = new MutableInt(CastInfos.RandomJet(Target));
        LostAP.setValue(LostAP.getValue() > Target.getAP() ? Target.getAP() : LostAP.getValue());
        CastInfos.DamageValue = Target.calculDodgeAPMP(CastInfos.Caster, LostAP.intValue(), false,CastInfos.Duration > 0);

        if (CastInfos.DamageValue != LostAP.intValue()) {
            Target.fight.sendToField(new GameActionFightDodgePointLossMessage(ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PA, Caster.ID, Target.ID, LostAP.getValue() - CastInfos.DamageValue));
        }

        if (CastInfos.DamageValue > 0) {
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.Sub_PA, this.CastInfos.SpellId, (short) this.CastInfos.SpellId, 0, null, this.CastInfos.Caster, null, false, StatsEnum.NOT_DISPELLABLE, CastInfos.DamageValue, CastInfos.SpellLevel, Duration, 0), this.Target);
            BuffStats.ApplyEffect(LostAP, null);
            this.Target.buff.addBuff(BuffStats);
            if (Target.ID == Target.fight.currentFighter.ID) {
                Target.fight.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, this.Caster.ID, Target.ID, (short) CastInfos.DamageValue));
            }
        }

        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
