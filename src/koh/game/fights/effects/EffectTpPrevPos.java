package koh.game.fights.effects;

import koh.game.fights.FightCell;
import koh.game.fights.Fighter;

import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_EXCHANGE_PLACES;
import static koh.protocol.client.enums.ActionIdEnum.ACTION_CHARACTER_TELEPORT_ON_SAME_MAP;

import koh.protocol.messages.game.actions.fight.GameActionFightExchangePositionsMessage;
import koh.protocol.messages.game.actions.fight.GameActionFightTeleportOnSameMapMessage;

/**
 *
 * @author Melancholia
 */
public class EffectTpPrevPos extends EffectBase {

    @Override
    public int applyEffect(EffectCast castInfos) {
        int toReturn = -1;
        for (final Fighter target : castInfos.targets) {
            if (target.getPreviousCellPos().isEmpty()) {
                continue;
            }
            final FightCell cell = target.getFight().getCell(target.getPreviousCellPos().get(target.getPreviousCellPos().size() - 1));

            if (cell != null && cell.canWalk()) {
                target.getFight().sendToField(new GameActionFightTeleportOnSameMapMessage(ACTION_CHARACTER_TELEPORT_ON_SAME_MAP, castInfos.caster.getID(), target.getID(), cell.Id));

                toReturn = target.setCell(cell);
            }
            else if(cell.hasFighter()) { //TELEFRAG
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
            if (toReturn != -1) {
                break;
            }
        }

        return toReturn;
    }

}
