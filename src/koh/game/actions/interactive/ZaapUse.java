package koh.game.actions.interactive;

import koh.game.actions.ZaapAction;
import koh.game.entities.actors.Player;
import koh.protocol.messages.connection.BasicNoOperationMessage;

/**
 *
 * @author Neo-Craft
 */
public class ZaapUse implements InteractiveAction {

    @Override
    public boolean isEnabled(Player actor) {
        return actor.getDishonor() <= 2;
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
        actor.getClient().addGameAction(new ZaapAction(actor));
        //actor.client.endGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
    }

    @Override
    public void leave(Player player, int element) {

    }

    @Override
    public void abort(Player player, int element) {

    }

}
