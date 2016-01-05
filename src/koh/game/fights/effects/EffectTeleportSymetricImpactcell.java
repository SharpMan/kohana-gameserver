package koh.game.fights.effects;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.FightCell;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

/**
 * Created by Melancholia on 1/4/16.
 */
public class EffectTeleportSymetricImpactcell extends EffectBase {

    @Override
    public int applyEffect(EffectCast CastInfos) {
        int toReturn = -1;
        FightCell cell;

        cell = CastInfos.caster.getFight().getCell(CastInfos.caster.getMapPoint().pointSymetry(MapPoint.fromCellId(CastInfos.cellId)).get_cellId());

        if (cell != null && cell.canWalk()) {
            CastInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, CastInfos.caster.getID(), CastInfos.caster.getID(), cell.Id));

            return CastInfos.caster.setCell(cell);
        } return -1;
    }
}
