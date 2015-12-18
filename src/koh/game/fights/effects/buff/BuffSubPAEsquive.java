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
        MutableInt LostAP = new MutableInt(CastInfos.RandomJet(Target));
        LostAP.setValue(LostAP.getValue() > Target.getAP() ? Target.getAP() : LostAP.getValue());
        CastInfos.DamageValue = Target.calculDodgeAPMP(CastInfos.caster, LostAP.intValue(), false,CastInfos.Duration > 0);

        if (CastInfos.DamageValue != LostAP.intValue()) {
            Target.fight.sendToField(new GameActionFightDodgePointLossMessage(ActionIdEnum.ACTION_FIGHT_SPELL_DODGED_PA, caster.getID(), Target.getID(), LostAP.getValue() - CastInfos.DamageValue));
        }

        if (CastInfos.DamageValue > 0) {
            BuffStats BuffStats = new BuffStats(new EffectCast(StatsEnum.Sub_PA, this.CastInfos.SpellId, (short) this.CastInfos.SpellId, 0, null, this.CastInfos.caster, null, false, StatsEnum.NOT_DISPELLABLE, CastInfos.DamageValue, CastInfos.SpellLevel, Duration, 0), this.Target);
            BuffStats.applyEffect(LostAP, null);
            this.Target.buff.addBuff(BuffStats);
            if (Target.getID() == Target.fight.currentFighter.getID()) {
                Target.fight.sendToField(new GameActionFightPointsVariationMessage(ActionIdEnum.ACTION_CHARACTER_ACTION_POINTS_LOST, this.caster.getID(), Target.getID(), (short) CastInfos.DamageValue));
            }
        }

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.getID(), (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
