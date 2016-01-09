package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholiax
 */
public class EffectTeleportSymetricFromCaster extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        int toReturn = -1;
        FightCell cell;
        for (Fighter target : castInfos.targets) {
            cell = castInfos.caster.getFight().getCell(target.getMapPoint().pointSymetry(castInfos.caster.getMapPoint()).get_cellId());

            if (cell != null && cell.canWalk()) {
                target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

                toReturn = target.setCell(cell);
                if (toReturn != -1) {
                    break;
                }
            }
        }
        return toReturn;
    }

}
