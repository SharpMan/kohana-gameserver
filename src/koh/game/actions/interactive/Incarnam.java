package koh.game.actions.interactive;

import koh.game.entities.actors.Player;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class Incarnam implements InteractiveAction {

    @Override
    public boolean isEnabled(Player actor) {
        return true;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public void execute(Player actor, int element) {
        if (!this.isEnabled(actor)) {
            actor.send(new BasicNoOperationMessage());
            return;
        }
        actor.teleport(80216068, 283);
        // actor.teleport(D2oDaoImpl.getBreed(actor.breed).spawnMaptype, 298);
    }

    @Override
    public void leave(Player player, int element) {

    }

    @Override
    public void abort(Player player, int element) {

    }

}
