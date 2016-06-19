package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffArmor extends BuffEffect {

    private final int JET;

    public BuffArmor(EffectCast CastInfos, Fighter target) {
        super(CastInfos, target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.JET = CastInfos.randomJet(target);
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        target.getStats().addBoost(StatsEnum.ADD_ARMOR, (JET * (100 + (castInfos.caster.getLevel() * 5)) / 100));
        return super.applyEffect(damageValue,damageInfos);
    }

    @Override
    public int removeEffect() {
        target.getStats().getEffect(StatsEnum.ADD_ARMOR).additionnal -= (JET * (100 + (castInfos.caster.getLevel() * 5)) / 100);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}
