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
        if (Client.character.getFight() == null || Client.character.getFighter() != Client.character.getFight().currentFighter) {
            return;
        }
        Client.character.getFight().acknowledgeAction();
        Client.sequenceMessage();

    }
}
