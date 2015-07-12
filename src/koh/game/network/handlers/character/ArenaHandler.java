package koh.game.network.handlers.character;

import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.game.context.roleplay.party.PartyInvitationArenaRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class ArenaHandler {

    @HandlerAttribute(ID = PartyInvitationArenaRequestMessage.M_ID)
    public static void HandlePartyInvitationArenaRequestMessage(WorldClient Client, PartyInvitationArenaRequestMessage Message) {
        PlayerController.SendServerMessage(Client, "Option non disponnible");
    }

}
