package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDispellSpell extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            
            if (Target.getBuff().dispell(castInfos.effect.value) == -3) {
                return -3;
            }

            Target.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, castInfos.caster.getID(), Target.getID(), castInfos.effect.value));
        }

        return -1;
    }

}