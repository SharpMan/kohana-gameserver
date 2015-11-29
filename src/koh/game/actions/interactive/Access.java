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
    public boolean isEnabled(Player Actor) {
        return true; //Todo Private Enclos
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

        Actor.Client.myExchange = new MountExchange(Actor.Client);
        Actor.Client.AddGameAction(new GameExchange(Actor, Actor.Client.myExchange));

    }

    @Override
    public void Leave(Player Actor, int Element) {

    }

    @Override
    public void Abort(Player player, int Element) {

    }

}
