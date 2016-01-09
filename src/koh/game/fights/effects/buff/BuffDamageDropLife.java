package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.game.fights.effects.EffectHeal;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffDamageDropLife extends BuffEffect {

    public BuffDamageDropLife(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_BEGINTURN, BuffDecrementType.TYPE_ENDTURN);
    }

    @Override
    public int applyEffect(MutableInt DamageJet, EffectCast DamageInfos) {
            //var Damage = this.castInfos.randomJet;

        // return EffectDamage.applyDamages(this.castInfos, this.target, ref Damage);
        int effectBase = DamageJet.getValue();
        //var DamageValuea = (target.currentLife / 100) * effectBase;
        MutableInt DamageValue = new MutableInt((castInfos.caster.currentLife / 100) * effectBase);
        if (EffectDamage.applyDamages(castInfos, castInfos.caster, DamageValue) == -3) {
            EffectHeal.applyHeal(castInfos, target, DamageValue);
            return -3;
        } else {
            return EffectHeal.applyHeal(castInfos, target, DamageValue);
        }

        //DamageValuea = (-DamageValuea);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.castInfos.randomJet(target)));
    }

}
