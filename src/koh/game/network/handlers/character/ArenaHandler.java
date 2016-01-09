package koh.game.network.handlers.character;

import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.game.context.roleplay.fight.arena.GameRolePlayArenaUpdatePlayerInfosMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyInvitationArenaRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class ArenaHandler {
    
    @HandlerAttribute(ID = 6301)
    public static void HandleGameRolePlayArenaUpdatePlayerInfosMessage(WorldClient Client , GameRolePlayArenaUpdatePlayerInfosMessage Message){
        PlayerController.sendServerMessage(Client, "Option non disponnible");
    }

    @HandlerAttribute(ID = PartyInvitationArenaRequestMessage.M_ID)
    public static void HandlePartyInvitationArenaRequestMessage(WorldClient Client, PartyInvitationArenaRequestMessage Message) {
        PlayerController.sendServerMessage(Client, "Option non disponnible");
    }

}
