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
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            if (Target.buff.debuff() == -3) {
                return -3;
            }

            Target.fight.sendToField(new GameActionFightDispellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.caster.getID(), Target.getID()));
        }

        return -1;
    }

}
