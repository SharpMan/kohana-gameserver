package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightModifyEffectsDurationMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDispellEffectDuration extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter Target : castInfos.targets) {
            short Jet = castInfos.randomJet(Target);
            if (Target.getBuff().decrementEffectDuration(Jet) == -3) {
                return -3;
            }

            Target.getFight().sendToField(new GameActionFightModifyEffectsDurationMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, castInfos.caster.getID(), Target.getID(), (short) -Jet));
        }

        return -1;
    }

}
