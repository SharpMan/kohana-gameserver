package koh.game.actions.interactive;

import koh.game.entities.actors.Player;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 *
 * @author Neo-Craft
 */
public class SavePos implements InteractiveAction {

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
        actor.savedMap = actor.currentMap.id;
        actor.savedCell = actor.cell.id;
        actor.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 6, new String[0]));

    }

    @Override
    public void leave(Player player, int element) {

    }

    @Override
    public void abort(Player player, int element) {

    }

}
