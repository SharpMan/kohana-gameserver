package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.game.fights.FighterSpell;
import koh.game.fights.effects.buff.BuffEffect;
import koh.game.fights.effects.buff.BuffSpellCoolDown;
import koh.protocol.messages.game.actions.fight.GameActionFightSpellCooldownVariationMessage;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_ADD_SPELL_COOLDOWN;

/**
 *
 * @author Neo-Craft
 */
public class EffectSpellCoolDown extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {

        for (Fighter target : castInfos.targets) {
            if(castInfos.duration == 0){
                applyCooldown(castInfos, target, true);
                continue;
            }
            final BuffEffect Buff = new BuffSpellCoolDown(castInfos, target);
            if (!target.getBuff().buffMaxStackReached(Buff)) {
                target.getBuff().addBuff(Buff);
                if (Buff.applyEffect(null, null) == -3) {
                    return -3;
                }
            }
        }
        return -1;
    }

    private static void applyCooldown(EffectCast castInfos, Fighter target, boolean boost){
        final FighterSpell.SpellInitialCooldown currentCooldown = target.getSpellsController().getInitialCooldown().get(castInfos.effect.diceNum);
        if(currentCooldown == null){
            target.getSpellsController().setCooldown(castInfos.effect.diceNum, (byte) castInfos.effect.value);
        }
        else{
            currentCooldown.initialCooldown /*+*/= (byte) castInfos.effect.value;
        }
        target.send(new GameActionFightSpellCooldownVariationMessage(ACTION_CHARACTER_ADD_SPELL_COOLDOWN, castInfos.caster.getID(), target.getID(), castInfos.effect.diceNum, /*target.getSpellsController().getInitialCooldown().get(castInfos.effect.diceNum).initialCooldown*/ castInfos.effect.value));

    }

}
