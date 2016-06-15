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
public class BuffDamagePerPM extends BuffEffect {

    public BuffDamagePerPM(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast DamageInfos) {
        int pas = this.castInfos.effect.diceNum;
        int val = this.castInfos.effect.diceSide;
        int nbr = (int) Math.floor((double) target.getUsedMP() / (double) pas);
        damageValue.setValue(val * nbr);
        //Poison Paralysant

        int inte = castInfos.caster.getStats().getTotal(StatsEnum.INTELLIGENCE);
        
        if (inte < 0) {
            inte = 0;
        }
        int pdom = castInfos.caster.getStats().getTotal(StatsEnum.ADD_DAMAGE_PERCENT);
        if (pdom < 0) {
            pdom = 0;
        }
        // on applique le boost
        // Ancienne formule : dgt = (int)(((100+inte+pdom)/100) *
        // dgt);
        damageValue.setValue((((100 + inte + pdom) / 100) * damageValue.getValue() /** 1.5*/));
        

        return EffectDamage.applyDamages(this.castInfos, this.target, damageValue);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
