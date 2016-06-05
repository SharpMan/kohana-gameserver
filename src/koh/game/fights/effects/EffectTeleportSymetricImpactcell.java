package koh.game.fights.effects;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;
import koh.protocol.messages.game.context.ShowCellMessage;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

/**
 * Created by Melancholia on 1/4/16.
 */
public class EffectTeleportSymetricImpactcell extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        for (Fighter target : castInfos.targets) {
            final MapPoint point = target.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.cellId));
            if(point == null){
                return -1;
            }
            target.getFight().sendToField(new ShowCellMessage(target.getID(),point.get_cellId()));
            //target.getFight().sendToField(new ShowCellMessage(target.getID(),(target.getMapPoint().pointSymetry(MapPoint.fromCellId(castInfos.caster.getCellId()))).get_cellId()));
            final FightCell cell = castInfos.caster.getFight().getCell(point.get_cellId());

            if (cell != null) {
                if(cell.canWalk()) {
                    castInfos.caster.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

                    final int result = target.setCell(cell);
                    if (result != -1) {
                        return result;
                    }
                }else if(cell.hasFighter()) { //TELEFRAG
                    final Fighter target2 = cell.getFighter();
                    final FightCell targetCell = target.getMyCell();
                    target2 .getFight().sendToField(new GameActionFightExchangePositionsMessage(ACTION_CHARACTER_EXCHANGE_PLACES, target2 .getID(), target.getID(), target.getCellId(), target2 .getCellId()));
                    target2 .setCell(null);
                    target.setCell(null);

                    if (target2.setCell(targetCell, false) == -3 || target.setCell(cell, false) == -3) {
                        return -3;
                    }

                    //Separated for false Sync wih piège call pushBackEffect
                    if (target2 .onCellChanged() == -3 || target.onCellChanged() == -3) {
                        return -3;
                    }

                    int result = target2 .getMyCell().onObjectAdded(target2 );
                    if (result == -3) {
                        return result;
                    }
                    result = target.getMyCell().onObjectAdded(target);
                    if (result == -3) {
                        return result;
                    }
                }
            }

        }
        return -1;

    }
}
