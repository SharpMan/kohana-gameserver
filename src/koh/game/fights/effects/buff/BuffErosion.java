package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffErosion extends BuffEffect {

    private final int JET;
    @Getter
    private int score;

    public BuffErosion(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        JET = this.castInfos.randomJet(target);
    }

    
    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        final float pdamage = JET / 100.00f;
        int buffValue = this.target.getMaxLife() - (int) (damageValue.getValue() * pdamage);
        score += damageValue.getValue() * pdamage;
        if (buffValue < 0) {
            buffValue = 0;
        }
        this.target.setLifeMax(buffValue);

        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public int removeEffect(){
        //this.target.setLifeMax(this.target.getMaxLife() + score);
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(JET));
    }
}
