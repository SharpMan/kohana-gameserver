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
    public int ApplyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.Targets) {
            
            if (Target.Buffs.Dispell(CastInfos.Effect.value) == -3) {
                return -3;
            }

            Target.Fight.sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.Caster.ID, Target.ID, CastInfos.Effect.value));
        }

        return -1;
    }

}