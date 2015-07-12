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
            if (Target.Buffs.DecrementEffectDuration(Jet) == -3) {
                return -3;
            }

            Target.Fight.sendToField(new GameActionFightModifyEffectsDurationMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.Caster.ID, Target.ID, (short) -Jet));
        }

        return -1;
    }

}
