package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.PartyRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.entities.kolissium.ArenaParty;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.client.enums.*;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.party.*;
import koh.protocol.messages.game.inventory.preset.InventoryPresetSaveMessage;
import koh.protocol.messages.game.inventory.preset.InventoryPresetSaveResultMessage;

/**
 *
 * @author Neo-Craft
 */
public class PartyHandler {
    
    @HandlerAttribute(ID = DungeonPartyFinderAvailableDungeonsRequestMessage.MESSAGE_ID)
    public static void handleDungeonPartyFinderAvailableDungeonsRequestMessage(WorldClient client , DungeonPartyFinderAvailableDungeonsRequestMessage message){
        client.send(new DungeonPartyFinderAvailableDungeonsMessage(new int[0]));
    }

    //TODO : GameFightOptionToggleMessage Just to group
    @HandlerAttribute(ID = PartyPledgeLoyaltyRequestMessage.M_ID)
    public static void handlePartyPledgeLoyaltyRequestMessage(WorldClient worldClient, PartyPledgeLoyaltyRequestMessage message) {
        if (worldClient.getParty() == null) {
            worldClient.send(new BasicNoOperationMessage());
            return;
        }
        //TODO : AutoDecliner
        worldClient.send(new PartyLoyaltyStatusMessage(worldClient.getParty().id, message.loyal));
    }


    @HandlerAttribute(ID = PartyNameSetRequestMessage.M_ID)
    public static void handlePartyNameSetRequestMessage(WorldClient client, PartyNameSetRequestMessage message) {
        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (!client.getParty().isChief(client.getCharacter())) {
            client.send(new PartyNameSetErrorMessage(client.getParty().id, PartyNameErrorEnum.PARTY_NAME_UNALLOWED_RIGHTS));
            return;
        }
        if (message.partyName.length() < 3 || message.partyName.length() > 20) {
            client.send(new PartyNameSetErrorMessage(client.getParty().id, PartyNameErrorEnum.PARTY_NAME_INVALID));
            return;
        }
        client.getParty().partyName = message.partyName;
        client.getParty().sendToField(new PartyNameUpdateMessage(client.getParty().id, client.getParty().partyName));
    }

    @HandlerAttribute(ID = PartyStopFollowRequestMessage.M_ID)
    public static void HandlePartyStopFollowRequestMessage(WorldClient client, PartyStopFollowRequestMessage message) {
        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (!client.getParty().isChief(client.getCharacter())) {
            PlayerController.sendServerMessage(client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }

    }

    @HandlerAttribute(ID = PartyFollowThisMemberRequestMessage.MESSAGE_ID)
    public static void HandlePartyFollowThisMemberRequestMessage(WorldClient client, PartyFollowThisMemberRequestMessage message) {

        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (!client.getParty().isChief(client.getCharacter())) {
            PlayerController.sendServerMessage(client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (client.getParty().getPlayerById(message.playerId) == null) {
            PlayerController.sendServerMessage(client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        if (message.enabled) {
            client.getParty().followAll(client.getParty().getPlayerById(message.playerId));
        } else {
            client.getParty().unFollowAll(client.getParty().getPlayerById(message.playerId));
        }

    }

    @HandlerAttribute(ID = PartyFollowMemberRequestMessage.MESSAGE_ID)
    public static void handlePartyFollowMemberRequestMessage(WorldClient client, PartyFollowMemberRequestMessage message) {
        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (client.getParty().getPlayerById(message.playerId) == null) {
            PlayerController.sendServerMessage(client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        client.getParty().getPlayerById(message.playerId).addFollower(client.getCharacter());
    }

    @HandlerAttribute(ID = PartyAbdicateThroneMessage.M_ID)
    public static void handlePartyAbdicateThroneMessage(WorldClient client, PartyAbdicateThroneMessage message) {
        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (!client.getParty().isChief(client.getCharacter())) {
            PlayerController.sendServerMessage(client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (client.getParty().getPlayerById(message.playerId) == null) {
            PlayerController.sendServerMessage(client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        client.getParty().updateLeader(client.getParty().getPlayerById(message.playerId));
    }

    @HandlerAttribute(ID = PartyKickRequestMessage.M_ID)
    public static void handlePartyKickRequestMessage(WorldClient client, PartyKickRequestMessage message) {
        if (client.getParty() == null) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        if (!client.getParty().isChief(client.getCharacter())) {
            PlayerController.sendServerMessage(client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (client.getParty().getPlayerById(message.playerId) == null) {
            PlayerController.sendServerMessage(client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        client.getParty().sendToField(new PartyMemberEjectedMessage(client.getParty().id, message.playerId, client.getCharacter().getID()));
        client.getParty().leave(client.getParty().getPlayerById(message.playerId), true);

    }

    @HandlerAttribute(ID = PartyLeaveRequestMessage.M_ID)
    public static void handlePartyLeaveRequestMessage(WorldClient client, PartyLeaveRequestMessage message) {
        if (!client.isGameAction(GameActionTypeEnum.GROUP)) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        try {
            client.endGameAction(GameActionTypeEnum.GROUP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = 5580)
    public static void handlePartyAcceptInvitationMessage(WorldClient client, PartyAcceptInvitationMessage message) {
        if (client.isGameAction(GameActionTypeEnum.GROUP)) {
            PlayerController.sendServerMessage(client, "Vous faites déjà partie d'un groupe ...");
            return;
        }
        PartyRequest gameParty = null;
        try {
            gameParty = client.getPartyRequest(message.partyId);
        } catch (Exception e) {
        }
        if (gameParty == null || !gameParty.accept()) {
            client.send(new BasicNoOperationMessage());
            return;
        }

    }

    @HandlerAttribute(ID = 6254)
    public static void handlePartyCancelInvitationMessage(WorldClient client, PartyCancelInvitationMessage message) {
        if(client.getPartyRequests() == null){
            client.send(new BasicNoOperationMessage());
            return;
        }
        final PartyRequest request = client.getPartyRequest(message.partyId, message.guestId);
        if (request != null) {
            request.abort();
            client.removePartyRequest(request);
        } else if (client.getParty() != null && client.getParty().isChief(client.getCharacter())) {
            client.getParty().abortRequest(client, message.guestId);
        } else {
            client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = PartyRefuseInvitationMessage.M_ID)
    public static void HandlePartyRefuseInvitationMessage(WorldClient Client, PartyRefuseInvitationMessage Message) {
        Party party = null;
        try {
            party = Client.getPartyRequest(Message.partyId).requester.getParty();
        } catch (Exception e) {
        }
        if (party == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.getPartyRequest(Message.partyId).declin();
    }

    @HandlerAttribute(ID = PartyInvitationDetailsRequestMessage.M_ID)
    public static void HandlePartyInvitationDetailsRequestMessage(WorldClient Client, PartyInvitationDetailsRequestMessage Message) {
        Party GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId).requester.getParty();
        } catch (Exception e) {
        }
        if (GameParty == null) {
            return;
        }
        Client.send(new PartyInvitationDetailsMessage(GameParty.id, GameParty.getType(), GameParty.partyName, Client.getPartyRequest(Message.partyId).requester.getCharacter().getID(), Client.getPartyRequest(Message.partyId).requester.getCharacter().getNickName(), GameParty.getChief().getID(), GameParty.toPartyInvitationMemberInformations(), GameParty.toPartyGuestInformations()));
    }

    @HandlerAttribute(ID = 5585)
    public static void handlePartyInvitationRequestMessage(WorldClient client, PartyInvitationRequestMessage message) {

        Player target = DAO.getPlayers().getCharacter(message.name);
        if (target == null) {
            client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_NOT_FOUND));
            return;
        }
        if (target.getClient() == null || target.getStatus() == PlayerStatusEnum.PLAYER_STATUS_AFK) {
            client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        if (!target.getClient().canGameAction(GameActionTypeEnum.GROUP)) {
            client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (!client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (!target.getClient().canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (client.getParty() != null && client.getParty().isFull()) {
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 302, new String[0]));
            client.send(new PartyCannotJoinErrorMessage(((GameParty) client.getGameAction(GameActionTypeEnum.GROUP)).party.id, PartyJoinErrorEnum.PARTY_JOIN_ERROR_NOT_ENOUGH_ROOM));
            return;
        }
        if (target.getAccount().accountData.ignore(client.getAccount().id)) {
            client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        final PartyRequest request = new PartyRequest(client, target.getClient());

        client.addPartyRequest(request);
        target.getClient().addPartyRequest(request);

        if (client.getParty() == null) {
            if(message instanceof PartyInvitationArenaRequestMessage)
                new ArenaParty(client.getCharacter(), target);
            else
                new Party(client.getCharacter(), target, PartyTypeEnum.PARTY_TYPE_CLASSICAL);
        } else {
            if (client.getParty().isRestricted() && !client.getParty().isChief(client.getCharacter())) {
                PlayerController.sendServerMessage(client, "Impossible d'inviter ce joueur, le groupe ne peut pas être modifié actuellement.");
                return;
            } else {
                client.getParty().addGuest(target);
            }
        }

        target.send(new PartyInvitationMessage(client.getParty().id, client.getParty().getType(), client.getParty().partyName, Party.MAX_PARTICIPANTS, client.getCharacter().getID(), client.getCharacter().getNickName(), target.getID()));

    }

}
