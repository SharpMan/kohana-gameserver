package koh.game.actions.interactive;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.ZaapAction;
import koh.game.entities.actors.Player;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class ZaapUse implements InteractiveAction {

    @Override
    public boolean isEnabled(Player Actor) {
        return Actor.Dishonor <= 0;
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
        Actor.Client.AddGameAction(new ZaapAction(Actor));
        //Actor.Client.EndGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
    }

    @Override
    public void Leave(Player Actor, int Element) {

    }

    @Override
    public void Abort(Player player, int Element) {

    }

}
