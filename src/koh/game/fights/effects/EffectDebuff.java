package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectDebuff extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            if (Target.getBuff().debuff() == -3) {
                return -3;
            }

            Target.getFight().sendToField(new GameActionFightDispellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.caster.getID(), Target.getID()));
        }

        return -1;
    }

}
