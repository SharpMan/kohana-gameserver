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
            
            if (Target.buff.dispell(CastInfos.Effect.value) == -3) {
                return -3;
            }

            Target.fight.sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.caster.getID(), Target.getID(), CastInfos.Effect.value));
        }

        return -1;
    }

}