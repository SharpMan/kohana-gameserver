package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporarySpellBoostEffect;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class BuffAddSpellRange extends BuffEffect {

    @Getter
    private final int spell;

    public BuffAddSpellRange(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.spell = this.castInfos.effect.diceNum;
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporarySpellBoostEffect(this.getId(), this.target.getID(), (short) this.duration, this.isDebuffable() ? FightDispellableEnum.DISPELLABLE : FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) this.castInfos.effect.value, (short) this.castInfos.effect.diceNum);
    }

}
