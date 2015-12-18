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
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        //FighterSpell.SpellinitialCooldown CurrentCooldown = this.Target.spellsController.myinitialCooldown.get(spell);
        this.Target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), Target.getID(), Spell, Value));
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        FighterSpell.SpellinitialCooldown CurrentCooldown = this.Target.spellsController.myinitialCooldown.get(Spell);
        if (CurrentCooldown != null) {
            this.Target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), Target.getID(), Spell, CurrentCooldown.initialCooldown + 1));
        }
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.Target.getID(), (short) this.CastInfos.Effect.duration, FightDispellableEnum.DISPELLABLE, this.CastInfos.SpellId, this.CastInfos.Effect.effectUid, 0, (short) this.CastInfos.Effect.diceNum, (short) this.CastInfos.Effect.diceSide, (short) this.CastInfos.Effect.value, (short) this.CastInfos.Effect.delay);
    }

}
