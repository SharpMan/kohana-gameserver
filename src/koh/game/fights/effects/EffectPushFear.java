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
        byte direction = Pathfinder.GetDirection(CastInfos.Caster.Fight.Map, CastInfos.Caster.CellId(), CastInfos.CellId);
        short targetFighterCell = Pathfinder.NextCell(CastInfos.Caster.CellId(), direction);

        Fighter target = CastInfos.Caster.Fight.GetFighterOnCell(targetFighterCell);
        if (target == null) {
            return -1;
        }
        short StartCell = target.CellId();
        int distance = Pathfinder.GoalDistance(CastInfos.Caster.Fight.Map, target.CellId(), CastInfos.CellId);
        FightCell currentCell = target.myCell;

        for (int i = 0; i < distance; i++) {
            FightCell nextCell = CastInfos.Caster.Fight.GetCell(Pathfinder.NextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.CanWalk()) {
                if (nextCell.HasObject(IFightObject.FightObjectType.OBJECT_TRAP)) {
                    target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, nextCell.Id));

                    return target.SetCell(nextCell);
                }
            } else {
                if (i != 0) {
                    target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, currentCell.Id));
                }

                return target.SetCell(currentCell);
            }

            currentCell = nextCell;
        }

        target.Fight.sendToField(new GameActionFightSlideMessage(CastInfos.Effect.effectId, CastInfos.Caster.ID, target.ID, StartCell, currentCell.Id));

        return target.SetCell(currentCell);
    }

}
