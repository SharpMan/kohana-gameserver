package koh.game.fights.effects.buff;

import koh.game.entities.item.EffectHelper;
import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightReduceDamagesMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Melancholia
 */
public class BuffReduceDamage extends BuffEffect {

    private final int JET;

    public BuffReduceDamage(EffectCast castInfos, Fighter target) {
        super(castInfos, target, BuffActiveType.ACTIVE_ATTACKED_AFTER_JET, BuffDecrementType.TYPE_ENDTURN);
        this.JET = castInfos.randomJet(target);
    }

    @Override
    public int applyEffect(MutableInt damageValue, EffectCast damageInfos) {
        if (EffectHelper.verifyEffectTrigger(damageInfos.caster, target, this.castInfos.spellLevel.getEffects(), damageInfos.effect, damageInfos.isCAC, this.castInfos.effect.triggers, damageInfos.cellId)) {
            final int armor = JET * (100 + (castInfos.caster.getLevel() * 5)) / 100;
            target.getFight().sendToField(new GameActionFightReduceDamagesMessage(ActionIdEnum.ACTION_CHARACTER_LIFE_LOST_MODERATOR, target.getID(), target.getID(), armor));
            damageValue.subtract(armor);
            //reduction = (reduction + (((pSpellInfo.targetLevel / 20) + 1) * (buff.effects as EffectInstanceInteger).value));
        }
        return super.applyEffect(damageValue, damageInfos);
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostEffect(this.getId(), this.target.getID(), (short) this.duration, FightDispellableEnum.DISPELLABLE, (short) this.castInfos.spellId, this.castInfos.getEffectUID(), this.castInfos.parentUID, (short) Math.abs(this.JET));
    }

}
