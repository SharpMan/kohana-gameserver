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
        this.Value = CastInfos.effect.value;
        this.Spell = CastInfos.effect.diceNum;
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        //FighterSpell.SpellinitialCooldown CurrentCooldown = this.target.spellsController.myinitialCooldown.get(spell);
        this.target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), target.getID(), Spell, Value));
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        FighterSpell.SpellinitialCooldown CurrentCooldown = this.target.getSpellsController().myinitialCooldown.get(Spell);
        if (CurrentCooldown != null) {
            this.target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), target.getID(), Spell, CurrentCooldown.initialCooldown + 1));
        }
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.GetId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.SpellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}
