package koh.game.fights.effects;

import koh.game.entities.environments.Pathfunction;
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
    public int applyEffect(EffectCast castInfos) { //TODO : Prise compte etat
        byte direction = Pathfunction.getDirection(castInfos.caster.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);
        short targetFighterCell = Pathfunction.nextCell(castInfos.caster.getCellId(), direction);

        Fighter target = castInfos.caster.getFight().getFighterOnCell(targetFighterCell);
        if (target == null) {
            return -1;
        }
        short StartCell = target.getCellId();
        int distance = Pathfunction.goalDistance(castInfos.caster.getFight().getMap(), target.getCellId(), castInfos.cellId);
        FightCell currentCell = target.getMyCell();

        for (int i = 0; i < distance; i++) {
            FightCell nextCell = castInfos.caster.getFight().getCell(Pathfunction.nextCell(currentCell.Id, direction));

            if (nextCell != null && nextCell.canWalk()) {
                if (nextCell.hasObject(IFightObject.FightObjectType.OBJECT_TRAP)) {
                    target.getFight().sendToField(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), StartCell, nextCell.Id));

                    return target.setCell(nextCell);
                }
            } else {
                if (i != 0) {
                    target.getFight().sendToField(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));
                }

                return target.setCell(currentCell);
            }

            currentCell = nextCell;
        }

        target.getFight().sendToField(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), StartCell, currentCell.Id));

        return target.setCell(currentCell);
    }

}
