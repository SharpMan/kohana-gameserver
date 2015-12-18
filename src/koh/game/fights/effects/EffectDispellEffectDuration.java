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
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            short Jet = CastInfos.RandomJet(Target);
            if (Target.buff.decrementEffectDuration(Jet) == -3) {
                return -3;
            }

            Target.fight.sendToField(new GameActionFightModifyEffectsDurationMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.caster.getID(), Target.getID(), (short) -Jet));
        }

        return -1;
    }

}
