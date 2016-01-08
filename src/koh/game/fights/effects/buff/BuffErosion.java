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
public class BuffErosion extends BuffEffect {

    private final int JET;

    public BuffErosion(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        JET = this.castInfos.randomJet(target);
    }

    
    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        float pdamage = JET / 100.00f;
        int BuffValue = this.target.getMaxLife() - (int) (DamageValue.getValue() * pdamage);
        if (BuffValue < 0) {
            BuffValue = 0;
        }
        this.target.setLifeMax(BuffValue);

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(JET));
    }
}
