package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 7/22/16.
 */
public class BuffShareDamages extends BuffEffect {


    @Getter
    private final long uid;

    public BuffShareDamages(EffectCast CastInfos, Fighter Target, long uid) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.uid = uid;
        this.duration+= 1;
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {

        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {

        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}