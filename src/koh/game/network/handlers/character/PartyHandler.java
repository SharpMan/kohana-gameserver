package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.PartyRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.client.enums.PartyJoinErrorEnum;
import koh.protocol.client.enums.PartyNameErrorEnum;
import koh.protocol.client.enums.PartyTypeEnum;
import koh.protocol.client.enums.PlayerStatusEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.party.*;

/**
 *
 * @author Neo-Craft
 */
public class PartyHandler {
    
    @HandlerAttribute(ID = DungeonPartyFinderAvailableDungeonsRequestMessage.MESSAGE_ID)
    public static void HandleDungeonPartyFinderAvailableDungeonsRequestMessage(WorldClient Client , DungeonPartyFinderAvailableDungeonsRequestMessage Message){
        Client.send(new DungeonPartyFinderAvailableDungeonsMessage(new int[0]));
    }

    //TODO : GameFightOptionToggleMessage Just to group
    @HandlerAttribute(ID = PartyPledgeLoyaltyRequestMessage.M_ID)
    public static void HandlePartyPledgeLoyaltyRequestMessage(WorldClient Client, PartyPledgeLoyaltyRequestMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        //TODO : AutoDecliner
        Client.send(new PartyLoyaltyStatusMessage(Client.getParty().id, Message.loyal));
    }

    @HandlerAttribute(ID = PartyNameSetRequestMessage.M_ID)
    public static void HandlePartyNameSetRequestMessage(WorldClient Client, PartyNameSetRequestMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.getParty().isChief(Client.getCharacter())) {
            Client.send(new PartyNameSetErrorMessage(Client.getParty().id, PartyNameErrorEnum.PARTY_NAME_UNALLOWED_RIGHTS));
            return;
        }
        if (Message.partyName.length() < 3 || Message.partyName.length() > 20) {
            Client.send(new PartyNameSetErrorMessage(Client.getParty().id, PartyNameErrorEnum.PARTY_NAME_INVALID));
            return;
        }
        Client.getParty().partyName = Message.partyName;
        Client.getParty().sendToField(new PartyNameUpdateMessage(Client.getParty().id, Client.getParty().partyName));
    }

    @HandlerAttribute(ID = PartyStopFollowRequestMessage.M_ID)
    public static void HandlePartyStopFollowRequestMessage(WorldClient Client, PartyStopFollowRequestMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.getParty().isChief(Client.getCharacter())) {
            PlayerController.sendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }

    }

    @HandlerAttribute(ID = PartyFollowThisMemberRequestMessage.MESSAGE_ID)
    public static void HandlePartyFollowThisMemberRequestMessage(WorldClient Client, PartyFollowThisMemberRequestMessage Message) {

        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.getParty().isChief(Client.getCharacter())) {
            PlayerController.sendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.getParty().getPlayerById(Message.playerId) == null) {
            PlayerController.sendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        if (Message.enabled) {
            Client.getParty().followAll(Client.getParty().getPlayerById(Message.playerId));
        } else {
            Client.getParty().unFollowAll(Client.getParty().getPlayerById(Message.playerId));
        }

    }

    @HandlerAttribute(ID = PartyFollowMemberRequestMessage.MESSAGE_ID)
    public static void HandlePartyFollowMemberRequestMessage(WorldClient Client, PartyFollowMemberRequestMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (Client.getParty().getPlayerById(Message.playerId) == null) {
            PlayerController.sendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.getParty().getPlayerById(Message.playerId).addFollower(Client.getCharacter());
    }

    @HandlerAttribute(ID = PartyAbdicateThroneMessage.M_ID)
    public static void HandlePartyAbdicateThroneMessage(WorldClient Client, PartyAbdicateThroneMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.getParty().isChief(Client.getCharacter())) {
            PlayerController.sendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.getParty().getPlayerById(Message.playerId) == null) {
            PlayerController.sendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.getParty().updateLeader(Client.getParty().getPlayerById(Message.playerId));
    }

    @HandlerAttribute(ID = PartyKickRequestMessage.M_ID)
    public static void HandlePartyKickRequestMessage(WorldClient Client, PartyKickRequestMessage Message) {
        if (Client.getParty() == null) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.getParty().isChief(Client.getCharacter())) {
            PlayerController.sendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.getParty().getPlayerById(Message.playerId) == null) {
            PlayerController.sendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.getParty().leave(Client.getParty().getPlayerById(Message.playerId), true);
        Client.getParty().sendToField(new PartyMemberEjectedMessage(Client.getParty().id, Message.playerId, Client.getCharacter().getID()));

    }

    @HandlerAttribute(ID = PartyLeaveRequestMessage.M_ID)
    public static void HandlePartyLeaveRequestMessage(WorldClient Client, PartyLeaveRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.GROUP)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        try {
            Client.endGameAction(GameActionTypeEnum.GROUP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = 5580)
    public static void HandlePartyAcceptInvitationMessage(WorldClient Client, PartyAcceptInvitationMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.GROUP)) {
            PlayerController.sendServerMessage(Client, "Vous faites déjà partie d'un groupe ...");
            return;
        }
        PartyRequest GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId);
        } catch (Exception e) {
        }
        if (GameParty == null || !GameParty.accept()) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

    }

    @HandlerAttribute(ID = 6254)
    public static void HandlePartyCancelInvitationMessage(WorldClient Client, PartyCancelInvitationMessage Message) {
        PartyRequest Req = Client.getPartyRequest(Message.partyId, Message.guestId);
        if (Req != null) {
            Req.abort();
            Client.removePartyRequest(Req);
        } else if (Client.getParty() != null && Client.getParty().isChief(Client.getCharacter())) {
            Client.getParty().abortRequest(Client, Message.guestId);
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = PartyRefuseInvitationMessage.M_ID)
    public static void HandlePartyRefuseInvitationMessage(WorldClient Client, PartyRefuseInvitationMessage Message) {
        Party GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId).requester.getParty();
        } catch (Exception e) {
        }
        if (GameParty == null) {
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
        Client.send(new PartyInvitationDetailsMessage(GameParty.id, GameParty.type, GameParty.partyName, Client.getPartyRequest(Message.partyId).requester.getCharacter().getID(), Client.getPartyRequest(Message.partyId).requester.getCharacter().getNickName(), GameParty.chief.getID(), GameParty.toPartyInvitationMemberInformations(), GameParty.toPartyGuestInformations()));
    }

    @HandlerAttribute(ID = 5585)
    public static void HandlePartyInvitationRequestMessage(WorldClient Client, Message Message) {

        Player target = DAO.getPlayers().getCharacter(((PartyInvitationRequestMessage) Message).name);
        if (target == null) {
            Client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_NOT_FOUND));
            return;
        }
        if (target.getClient() == null || target.getStatus() == PlayerStatusEnum.PLAYER_STATUS_AFK) {
            Client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        if (!target.getClient().canGameAction(GameActionTypeEnum.GROUP)) {
            Client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (!Client.canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (!target.getClient().canGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (Client.getParty() != null && Client.getParty().isFull()) {
            Client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 302, new String[0]));
            Client.send(new PartyCannotJoinErrorMessage(((GameParty) Client.getGameAction(GameActionTypeEnum.GROUP)).party.id, PartyJoinErrorEnum.PARTY_JOIN_ERROR_NOT_ENOUGH_ROOM));
            return;
        }
        if (target.getAccount().accountData.ignore(Client.getAccount().id)) {
            Client.send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        PartyRequest Request = new PartyRequest(Client, target.getClient());

        Client.addPartyRequest(Request);
        target.getClient().addPartyRequest(Request);

        if (Client.getParty() == null) {
            new Party(Client.getCharacter(), target);
        } else {
            if (Client.getParty().restricted && !Client.getParty().isChief(Client.getCharacter())) {
                PlayerController.sendServerMessage(Client, "Impossible d'inviter ce joueur, le groupe ne peut pas être modifié actuellement.");
                return;
            } else {
                Client.getParty().addGuest(target);
            }
        }

        target.send(new PartyInvitationMessage(Client.getParty().id, PartyTypeEnum.PARTY_TYPE_CLASSICAL, Client.getParty().partyName, Party.MAX_PARTICIPANTS, Client.getCharacter().getID(), Client.getCharacter().getNickName(), target.getID()));

    }

}
