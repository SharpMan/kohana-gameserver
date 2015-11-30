package koh.game.fights.effects.buff;

import koh.game.fights.Fighter;
import koh.game.fights.FighterSpell;
import koh.game.fights.effects.EffectCast;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_ADD_SPELL_COOLDOWN;
import koh.protocol.client.enums.FightDispellableEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSpellCooldownVariationMessage;
import koh.protocol.types.game.actions.fight.AbstractFightDispellableEffect;
import koh.protocol.types.game.actions.fight.FightTriggeredEffect;
import org.apache.commons.lang3.mutable.MutableInt;

/**
 *
 * @author Neo-Craft
 */
public class BuffSpellCoolDown extends BuffEffect {

    public int Value, Spell;

    public BuffSpellCoolDown(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_ENDTURN);
        this.Value = CastInfos.Effect.value;
        this.Spell = CastInfos.Effect.diceNum;
    }

    @Override
    public int ApplyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        //FighterSpell.SpellinitialCooldown CurrentCooldown = this.Target.SpellsController.myinitialCooldown.get(Spell);
        this.Target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.Caster.ID, Target.ID, Spell, Value));
        return super.ApplyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int RemoveEffect() {
        FighterSpell.SpellinitialCooldown CurrentCooldown = this.Target.SpellsController.myinitialCooldown.get(Spell);
        if (CurrentCooldown != null) {
            this.Target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.Caster.ID, Target.ID, Spell, CurrentCooldown.initialCooldown + 1));
        }
        return super.RemoveEffect();
    }

    @Override
    public AbstractFightDispellableEffect GetAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.ID, (short) this.CastInfos.Effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.delay);
    }

}
