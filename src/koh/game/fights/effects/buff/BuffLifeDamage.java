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
        //var Damage = this.CastInfos.RandomJet;

        // return EffectDamage.ApplyDamages(this.CastInfos, this.Target, ref Damage);
        int effectBase = CastInfos.RandomJet(Target);
        MutableInt DamageValuea = new MutableInt((Target.currentLife / 100) * effectBase);
        //DamageValuea = (-DamageValuea);
        return EffectDamage.ApplyDamages(this.CastInfos, this.Target, DamageValuea);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.getID(), (short) this.Duration, FightDispellableEnum.DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.CastInfos.RandomJet(Target)));
    }
}
