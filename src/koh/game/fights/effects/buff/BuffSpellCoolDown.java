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

    public int value, spell;

    public BuffSpellCoolDown(EffectCast CastInfos, Fighter Target) {
        super(CastInfos, Target, BuffActiveType.ACTIVE_STATS, BuffDecrementType.TYPE_BEGINTURN);
        this.value = CastInfos.effect.value;
        this.spell = CastInfos.effect.diceNum;
    }

    @Override
    public int applyEffect(MutableInt DamageValue, EffectCast DamageInfos) {
        //FighterSpell.SpellinitialCooldown CurrentCooldown = this.target.spellsController.initialCooldown.get(spell);
        this.target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), target.getID(), spell, value));
        return super.applyEffect(DamageValue, DamageInfos);
    }

    @Override
    public int removeEffect() {
        final FighterSpell.SpellinitialCooldown currentCooldown = this.target.getSpellsController().getInitialCooldown().get(spell);
        if (currentCooldown != null) {
            this.target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, this.caster.getID(), target.getID(), spell, currentCooldown.initialCooldown + 1));
        }
        return super.removeEffect();
    }

    @Override
    public AbstractFightDispellableEffect getAbstractFightDispellableEffect() {
        return new FightTriggeredEffect(this.getId(), this.target.getID(), (short) this.castInfos.effect.duration, FightDispellableEnum.DISPELLABLE, this.castInfos.spellId, this.castInfos.effect.effectUid, 0, (short) this.castInfos.effect.diceNum, (short) this.castInfos.effect.diceSide, (short) this.castInfos.effect.value, (short) this.castInfos.effect.delay);
    }

}
