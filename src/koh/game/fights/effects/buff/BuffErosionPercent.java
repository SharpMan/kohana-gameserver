package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffErosionPercent extends BuffEffect {

    public BuffErosionPercent(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
    }

    public int Added, Jet;

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        this.Jet = this.CastInfos.RandomJet(Target);
        float pdamage = Jet / 100.00f;
        if (this.Target.getMaxLife() - (int) (this.Target.getMaxLife() * pdamage) > 0) {
            Added = (int) (this.Target.getMaxLife() * pdamage);
        }
        this.Target.setLifeMax(this.Target.getMaxLife() - Added);

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.Target.setLifeMax(this.Target.getMaxLife() + Added);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.GetId(), this.Target.ID, (short) this.Duration, FightDispellableEnum.DISPELLABLE, (short) this.CastInfos.SpellId, this.CastInfos.GetEffectUID(), this.CastInfos.ParentUID, (short) Math.abs(Jet));
    }
}
