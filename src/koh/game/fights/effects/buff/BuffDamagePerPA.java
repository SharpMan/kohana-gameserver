package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffDamagePerPA extends BuffEffect {

    public BuffDamagePerPA(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        int pas = this.CastInfos.Effect.diceNum;
        int val = this.CastInfos.Effect.diceSide;
        int nbr = (int) Math.floor((double) target.usedAP / (double) pas);
        DamageValue.setValue(val * nbr);
        //Poison Paralysant

        int inte = 0;
        if (CastInfos.EffectType == StatsEnum.PA_USED_LOST_X_PDV) {
            inte += CastInfos.caster.stats.getTotal(StatsEnum.Agility);
        } else /*if (CastInfos.EffectType == StatsEnum.Lose_PV_By_Using_PA)*/ {
            inte += CastInfos.caster.stats.getTotal(StatsEnum.Intelligence);
        }
        if (inte < 0) {
            inte = 0;
        }
        int pdom = CastInfos.caster.stats.getTotal(StatsEnum.AddDamagePercent);
        if (pdom < 0) {
            pdom = 0;
        }
        // on applique le boost
        // Ancienne formule : dgt = (int)(((100+inte+pdom)/100) *
        // dgt);
        DamageValue.setValue((((100 + inte + pdom) / 100) * DamageValue.getValue() * 1.5));

        return EffectDamage.ApplyDamages(this.CastInfos, this.target, DamageValue);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.Duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
