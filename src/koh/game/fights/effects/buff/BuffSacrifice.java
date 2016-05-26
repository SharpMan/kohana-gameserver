package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectBase;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.game.fights.effects.EffectLifeSteal;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
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
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        //TODO trigger
        if (damageInfos.isReflect || damageInfos.isReturnedDamages || damageInfos.isPoison) {
            return -1;
        }
        // mort
        if (caster.isDead()) {
            //target.buff.RemoveBuff(this);
            return -1;
        }

        damageValue.setValue(0);

        damageInfos.isReturnedDamages = true;

        if(EffectLifeSteal.isStealingEffect(damageInfos.effectType)){
            return EffectLifeSteal.applyLifeSteal(damageInfos,castInfos.caster,new MutableInt(damageInfos.randomJet(caster)));
        }

        return EffectDamage.applyDamages(damageInfos, castInfos.caster, new MutableInt(damageInfos.randomJet(caster)));

    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}
