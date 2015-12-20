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
            //var Damage = this.CastInfos.RandomJet;

        // return EffectDamage.ApplyDamages(this.CastInfos, this.target, ref Damage);
        int effectBase = DamageJet.getValue();
        //var DamageValuea = (target.currentLife / 100) * effectBase;
        MutableInt DamageValue = new MutableInt((CastInfos.caster.currentLife / 100) * effectBase);
        if (EffectDamage.ApplyDamages(CastInfos, CastInfos.caster, DamageValue) == -3) {
            EffectHeal.ApplyHeal(CastInfos, target, DamageValue);
            return -3;
        } else {
            return EffectHeal.ApplyHeal(CastInfos, target, DamageValue);
        }

        //DamageValuea = (-DamageValuea);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.Duration, FightDispellableEnum.DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.CastInfos.RandomJet(target)));
    }

}
