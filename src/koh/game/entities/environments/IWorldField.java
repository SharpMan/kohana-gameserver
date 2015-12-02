package koh.game.entities.environments;

import koh.game.entities.actors.IGameActor;
import koh.game.entities.maps.pathfinding.Path;

/**
 *
 * @author Neo-Craft
 */
public interface IWorldField {

    void actorMoved(Path Path, IGameActor Actor, short newCell, byte newDirection);
}
