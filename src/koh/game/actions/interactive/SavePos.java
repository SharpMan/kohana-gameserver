package koh.game.actions.interactive;

import koh.game.controllers.PlayerController;
import koh.game.dao.MapDAO;
import koh.game.entities.actors.Player;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 *
 * @author Neo-Craft
 */
public class SavePos implements InteractiveAction {

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
        Actor.SavedMap = Actor.CurrentMap.Id;
        Actor.SavedCell = Actor.Cell.Id;
        Actor.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 6, new String[0]));

    }

    @Override
    public void Leave(Player Actor, int Element) {

    }

    @Override
    public void Abort(Player player, int Element) {

    }

}
