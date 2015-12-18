package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.effects.EffectCast;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CARRY_CHARACTER;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightCarryCharacterMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTemporaryBoostStateEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffPorteur extends BuffEffect {

    public BuffPorteur(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_ENDMOVE, BuffDecrementType.TYPE_ENDMOVE);
        this.Duration = -1;
        CastInfos.caster.states.fakeState(FightStateEnum.Porteur, true);
        this.CastInfos.EffectType = StatsEnum.Add_State;
        this.caster.fight.sendToField(new GameActionFightCarryCharacterMessage(ACTION_CARRY_CHARACTER, caster.getID(),Target.getID(), caster.getCellId()));
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTemporaryBoostStateEffect(this.GetId(), this.caster.getID(), (short) this.Duration, FightDispellableEnum.REALLY_NOT_DISPELLABLE, (short) this.CastInfos.SpellId, (short)/*this.CastInfos.GetEffectUID()*/ 2, this.CastInfos.ParentUID, (short) 1, (short) 3);
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        // Si effet finis
        if (!this.Target.states.hasState(FightStateEnum.Porté)) {
            this.Duration = 0;
            return -1;
        }

        // On affecte la meme cell pour la cible porté
        return this.Target.setCell(this.caster.myCell);
    }

    @Override
    public int removeEffect() {
        CastInfos.caster.states.fakeState(FightStateEnum.Porteur, false);
        this.Duration = 0;
        return super.removeEffect();
    }

}
