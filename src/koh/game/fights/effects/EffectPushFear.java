package koh.game.fights.effects;

import koh.game.entities.environments.Pathfinder;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.protocol.messages.game.actions.fight.GameActionFightSlideMessage;

/**
 *
 * @author Neo-Craft
 */
public class EffectPushFear extends EffectBase {

    @Override
    public int ApplyEffect(EffectCast CastInfos) { //TODO : Prise compte etat
        byte direction = Pathfinder.getDirection(CastInfos.caster.fight.map, CastInfos.caster.getCellId(), CastInfos.CellId);
        short targetFighterCell = Pathfinder.nextCell(CastInfos.caster.getCellId(), direction);

        Fighter target = CastInfos.caster.fight.getFighterOnCell(targetFighterCell);
        if (target == null) {
            return -1;
        }
        short StartCell = target.getCellId();
        int distance = Pathfinder.getGoalDistance(CastInfos.caster.fight.map, target.getCellId(), CastInfos.CellId);
        FightCell currentCell = target.myCell;

        for (int i = 0; i < distance; i++) {
            FightCell nextCell = CastInfos.caster.fight.getCell(Pathfinder.nextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.CanWalk()) {
                if (nextCell.HasObject(IFightObject.FightObjectType.OBJECT_TRAP)) {
                    target.fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, nextCell.Id));

                    return target.setCell(nextCell);
                }
            } else {
                if (i != 0) {
                    target.fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));
                }

                return target.setCell(currentCell);
            }

            currentCell = nextCell;
        }

        target.fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

        return target.setCell(currentCell);
    }

}
