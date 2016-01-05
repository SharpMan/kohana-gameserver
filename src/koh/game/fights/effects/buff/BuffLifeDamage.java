package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffLifeDamage extends BuffEffect {

    public BuffLifeDamage(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_BEGINTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        //var Damage = this.castInfos.randomJet;

        // return EffectDamage.applyDamages(this.castInfos, this.target, ref Damage);
        int effectBase = castInfos.randomJet(target);
        MutableInt DamageValuea = new MutableInt((target.currentLife / 100) * effectBase);
        //DamageValuea = (-DamageValuea);
        return EffectDamage.applyDamages(this.castInfos, this.target, DamageValuea);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.castInfos.randomJet(target)));
    }
}
