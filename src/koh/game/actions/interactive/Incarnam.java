package koh.game.actions.interactive;

import koh.game.entities.actors.Player;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class Incarnam implements InteractiveAction {

    @Override
    public boolean isEnabled(Player Actor) {
        return true;
    }

    @Override
    public int GetDuration() {
        return 0;
    }

    @Override
    public void Execute(Player Actor, int Element) {
        if (!this.isEnabled(Actor)) {
            Actor.Send(new BasicNoOperationMessage());
            return;
        }
        Actor.teleport(80216068, 283);
        // Actor.teleport(D2oDaoImpl.getBreed(Actor.Breed).spawnMaptype, 298);
    }

    @Override
    public void Leave(Player Actor, int Element) {

    }

    @Override
    public void Abort(Player player, int Element) {

    }

}
