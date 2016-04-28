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
    public int applyEffect(EffectCast castInfos) {
        final MapPoint point = castInfos.caster.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.cellId));
        if(point == null){
            return -1;
        }

        final FightCell cell = castInfos.caster.getFight().getCell(point.get_cellId());

        if (cell != null && cell.canWalk()) {
            castInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), castInfos.caster.getID(), cell.Id));

            return castInfos.caster.setCell(cell);
        } return -1;
    }
}
