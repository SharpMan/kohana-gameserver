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

    private final int JET;

    public BuffArmor(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.JET = CastInfos.randomJet(Target);
        Target.getStats().addBoost(StatsEnum.AddArmor, (JET * (100 + (CastInfos.caster.getLevel() * 5)) / 100));
    }

    @Override
    public int removeEffect() {
        target.getStats().getEffect(StatsEnum.AddArmor).additionnal -= (JET * (100 + (castInfos.caster.getLevel() * 5)) / 100);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}
