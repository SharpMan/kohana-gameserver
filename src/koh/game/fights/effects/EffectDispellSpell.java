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
    public int applyEffect(EffectCast CastInfos) {
        for (Fighter Target : CastInfos.targets) {
            
            if (Target.getBuff().dispell(CastInfos.effect.value) == -3) {
                return -3;
            }

            Target.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_CHARACTER_REMOVE_ALL_EFFECTS, CastInfos.caster.getID(), Target.getID(), CastInfos.effect.value));
        }

        return -1;
    }

}