package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Created by Melancholia on 3/29/16.
 */
public class BuffCastSpellOnPortal extends BuffEffect {


    public BuffCastSpellOnPortal(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.duration++;

    }

    public int applyEffect(Fighter target) {
        super.applyEffect(null,null);
        this.target.getFight().affectSpellTo(castInfos.caster, castInfos.caster, castInfos.effect.diceSide, castInfos.effect.diceNum);
        return -1;
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}
