package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.game.fights.effects.EffectEnableBomb;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffEnableBomb  extends BuffEffect {

    public BuffEnableBomb(EffectCast castInfos, Fighter Target) {
        super(castInfos, Target, BuffActiveType.ACTIVE_ON_DIE, BuffDecrementType.TYPE_ENDTURN);
        if(castInfos.effect.triggers.equalsIgnoreCase("TE")){
            this.activeType = BuffActiveType.ACTIVE_ENDTURN;
        }
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        return EffectEnableBomb.explose(target, castInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) 0/*(this.castInfos.effect.delay)*/);
    }

}
