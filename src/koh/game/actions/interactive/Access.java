package koh.game.actions.interactive;

import koh.game.actions.GameExchange;
import koh.game.entities.actors.Player;
import koh.game.exchange.MountExchange;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class Access implements InteractiveAction {

    @Override
    public boolean isEnabled(Player actor) {
        return true; //Todo Private Enclos
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

        actor.getClient().myExchange = new MountExchange(actor.getClient());
        actor.getClient().addGameAction(new GameExchange(actor, actor.getClient().myExchange));

    }

    @Override
    public void leave(Player player, int element) {

    }

    @Override
    public void abort(Player player, int element) {

    }

}
