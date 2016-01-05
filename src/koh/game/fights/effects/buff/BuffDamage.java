package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffDamage extends BuffEffect {

    public BuffDamage(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_BEGINTURN, BuffDecrementType.TYPE_ENDTURN);
    }
    
    //SendGameActionFightDispellEffectMessag

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt Damage = new MutableInt(this.castInfos.randomJet(target));

        return EffectDamage.applyDamages(this.castInfos, this.target, Damage);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.duration, (byte) 0, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
