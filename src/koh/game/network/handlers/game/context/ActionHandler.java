package koh.game.network.handlers.game.context;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.game.actions.GameActionAcknowledgementMessage;

/**
 *
 * @author Neo-Craft
 */
public class ActionHandler {

    @HandlerAttribute(ID = 957)
    public static void HandleGameActionAcknowledgementMessage(WorldClient Client, GameActionAcknowledgementMessage Message) {
        if (Client.getCharacter() == null || Client.getCharacter().getFight() == null || Client.getCharacter().getFighter() != null || Client.getCharacter().getFighter() != Client.getCharacter().getFight().getCurrentFighter()) {
            return;
        }
        Client.getCharacter().getFight().acknowledgeAction();
        Client.sequenceMessage();

    }
}
