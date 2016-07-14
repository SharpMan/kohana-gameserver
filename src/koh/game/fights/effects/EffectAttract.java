package koh.game.fights.effects;

import koh.game.entities.actors.Player;
import koh.game.entities.environments.Pathfunction;
import koh.game.fights.FightCell;
import koh.game.fights.Fighter;
import koh.game.fights.IFightObject;
import koh.game.fights.effects.buff.BuffPorteur;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.messages.game.actions.fight.GameActionFightSlideMessage;

import java.util.Stack;

/**
 * Created by Melancholia on 6/23/16.
 */
public class EffectAttract extends EffectBase {


    @Override
    public int applyEffect(EffectCast castInfos) {

        final byte direction = Pathfunction.getDirection(castInfos.getFight().getMap(), castInfos.caster.getCellId(), castInfos.cellId);

        FightCell nextCell = castInfos.getCell();
        final Stack<FightCell> trajet = new Stack<>();

        while (true) {
            nextCell = castInfos.getFight().getCell(Pathfunction.nextCell(nextCell.Id, direction));
            if (nextCell == null) {
                break;
            }
            if (nextCell.hasFighter()) {
                final Fighter target = nextCell.getFighter();
                final FightCell startCell = target.getMyCell();
                while (!trajet.isEmpty()) {
                    final FightCell poopedCell = trajet.pop();
                    if (poopedCell != null && poopedCell.canWalk()) {
                        if (poopedCell.hasObject(IFightObject.FightObjectType.OBJECT_TRAP)) {
                            castInfos.getFight().observable$Stream(p -> target.isVisibleFor(p)).forEach(p -> p.send(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), startCell.Id, poopedCell.Id)));
                            target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(startCell.getId())));
                            return target.setCell(poopedCell);
                        }
                    } else {
                        target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(startCell.getId())));
                        for (Player player : castInfos.getFight().observable$Stream(p -> target.isVisibleFor(p))) {
                            player.send(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), startCell.getId(), poopedCell.Id));
                        }
                        return target.setCell(poopedCell);

                    }
                }

                castInfos.getFight()
                        .observable$Stream(p -> target.isVisibleFor(p))
                        .forEach(p -> p.send(new GameActionFightSlideMessage(castInfos.effect.effectId, castInfos.caster.getID(), target.getID(), startCell.getId(), castInfos.cellId)));
                target.getBuff().getAllBuffs().filter(x -> x instanceof BuffPorteur && x.duration != 0).forEach(x -> x.target.setCell(target.getFight().getCell(startCell.getId())));

                return target.setCell(castInfos.getCell());
            } else {
                trajet.add(nextCell);
            }
        }


        return -1;
    }
}
