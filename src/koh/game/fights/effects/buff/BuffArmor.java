package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;

/**
 *
 * @author Neo-Craft
 */
public class BuffArmor extends BuffEffect {

    private int Jet;

    public BuffArmor(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.Jet = CastInfos.RandomJet(Target);
        Target.stats.addBoost(StatsEnum.AddArmor, (Jet * (100 + (CastInfos.caster.getLevel() * 5)) / 100));
    }

    @Override
    public int removeEffect() {
        target.stats.getEffect(StatsEnum.AddArmor).additionnal -= (Jet * (100 + (CastInfos.caster.getLevel() * 5)) / 100);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.target.getID(), (short) this.Duration, FightDispellableEnum.DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(this.Jet));
    }

}
