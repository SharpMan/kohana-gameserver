package koh.game.fights.effects;

import koh.game.fights.Fighter;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.FightStateEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightDispellSpellMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectLostState extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            target.getStates().removeState(FightStateEnum.valueOf(castInfos.effect.value));
            target.getFight().sendToField(new GameActionFightDispellSpellMessage(ActionIdEnum.ACTION_FIGHT_UNSET_STATE, castInfos.caster.getID(), target.getID(), castInfos.spellId));
        }

        return -1;
    }

}
