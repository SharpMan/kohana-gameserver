package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectDamage;
import koh.game.fights.effects.EffectLifeSteal;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffLifeSteal extends BuffEffect {

    public BuffLifeSteal(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_POST_JET_TRAP, BuffDecrementType.TYPE_ENDTURN);
    }

    //SendGameActionFightDispellEffectMessag
    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        MutableInt Damage = new MutableInt(this.CastInfos.RandomJet(Target));

        return EffectLifeSteal.ApplyLifeSteal(this.CastInfos, this.Target, Damage);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.getID(), (short) this.Duration, (byte) 0, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) 0/*(this.CastInfos.Effect.delay)*/);
    }

}
