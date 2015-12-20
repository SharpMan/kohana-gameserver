package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholiax
 */
public class EffectTeleportSymetric extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) {
        int toReturn = -1;
        FightCell cell;
        for (Fighter target : CastInfos.Targets) {
            cell = CastInfos.caster.getFight().getCell(target.getMapPoint().pointSymetry(CastInfos.caster.getMapPoint()).get_cellId());

            if (cell != null && cell.IsWalkable()) {
                target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.caster.getID(), target.getID(), cell.Id));

                toReturn = target.setCell(cell);
                if (toReturn != -1) {
                    break;
                }
            }
        }
        return toReturn;
    }

}
