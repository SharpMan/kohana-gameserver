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
        this.Jet = this.castInfos.randomJet(target);
        float pdamage = Jet / 100.00f;
        if (this.target.getMaxLife() - (int) (this.target.getMaxLife() * pdamage) > 0) {
            Added = (int) (this.target.getMaxLife() * pdamage);
        }
        this.target.setLifeMax(this.target.getMaxLife() - Added);

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        this.target.setLifeMax(this.target.getMaxLife() + Added);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(Jet));
    }
}
