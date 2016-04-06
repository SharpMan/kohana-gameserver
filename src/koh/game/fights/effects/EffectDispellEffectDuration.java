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
        for (Fighter target : castInfos.targets) {
            final short jet = castInfos.randomJet(target);
            if (target.getBuff().decrementEffectDuration(jet) == -3) {
                return -3;
            }

            target.getFight().sendToField(new GameActionFightModifyEffectsDurationMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, castInfos.caster.getID(), target.getID(), (short) -jet));
        }

        return -1;
    }

}
