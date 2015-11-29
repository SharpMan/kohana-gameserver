package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.PartyRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.PlayerDAO;
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
        Client.Send(new DungeonPartyFinderAvailableDungeonsMessage(new int[0]));
    }

    //TODO : GameFightOptionToggleMessage Just to group
    @HandlerAttribute(ID = PartyPledgeLoyaltyRequestMessage.M_ID)
    public static void HandlePartyPledgeLoyaltyRequestMessage(WorldClient Client, PartyPledgeLoyaltyRequestMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        //TODO : AutoDecliner
        Client.Send(new PartyLoyaltyStatusMessage(Client.GetParty().ID, Message.loyal));
    }

    @HandlerAttribute(ID = PartyNameSetRequestMessage.M_ID)
    public static void HandlePartyNameSetRequestMessage(WorldClient Client, PartyNameSetRequestMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.GetParty().isChief(Client.Character)) {
            Client.Send(new PartyNameSetErrorMessage(Client.GetParty().ID, PartyNameErrorEnum.PARTY_NAME_UNALLOWED_RIGHTS));
            return;
        }
        if (Message.partyName.length() < 3 || Message.partyName.length() > 20) {
            Client.Send(new PartyNameSetErrorMessage(Client.GetParty().ID, PartyNameErrorEnum.PARTY_NAME_INVALID));
            return;
        }
        Client.GetParty().PartyName = Message.partyName;
        Client.GetParty().sendToField(new PartyNameUpdateMessage(Client.GetParty().ID, Client.GetParty().PartyName));
    }

    @HandlerAttribute(ID = PartyStopFollowRequestMessage.M_ID)
    public static void HandlePartyStopFollowRequestMessage(WorldClient Client, PartyStopFollowRequestMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.GetParty().isChief(Client.Character)) {
            PlayerController.SendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }

    }

    @HandlerAttribute(ID = PartyFollowThisMemberRequestMessage.MESSAGE_ID)
    public static void HandlePartyFollowThisMemberRequestMessage(WorldClient Client, PartyFollowThisMemberRequestMessage Message) {

        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.GetParty().isChief(Client.Character)) {
            PlayerController.SendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.GetParty().getPlayerById(Message.playerId) == null) {
            PlayerController.SendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        if (Message.enabled) {
            Client.GetParty().FollowAll(Client.GetParty().getPlayerById(Message.playerId));
        } else {
            Client.GetParty().UnFollowAll(Client.GetParty().getPlayerById(Message.playerId));
        }

    }

    @HandlerAttribute(ID = PartyFollowMemberRequestMessage.MESSAGE_ID)
    public static void HandlePartyFollowMemberRequestMessage(WorldClient Client, PartyFollowMemberRequestMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (Client.GetParty().getPlayerById(Message.playerId) == null) {
            PlayerController.SendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.GetParty().getPlayerById(Message.playerId).addFollower(Client.Character);
    }

    @HandlerAttribute(ID = PartyAbdicateThroneMessage.M_ID)
    public static void HandlePartyAbdicateThroneMessage(WorldClient Client, PartyAbdicateThroneMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.GetParty().isChief(Client.Character)) {
            PlayerController.SendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.GetParty().getPlayerById(Message.playerId) == null) {
            PlayerController.SendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.GetParty().UpdateLeader(Client.GetParty().getPlayerById(Message.playerId));
    }

    @HandlerAttribute(ID = PartyKickRequestMessage.M_ID)
    public static void HandlePartyKickRequestMessage(WorldClient Client, PartyKickRequestMessage Message) {
        if (Client.GetParty() == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        if (!Client.GetParty().isChief(Client.Character)) {
            PlayerController.SendServerMessage(Client, "Erreur : Vous n'êtes pas chef du groupe");
            return;
        }
        if (Client.GetParty().getPlayerById(Message.playerId) == null) {
            PlayerController.SendServerMessage(Client, "Erreur : Ce joueur ne fait pas partie du groupe");
            return;
        }
        Client.GetParty().Leave(Client.GetParty().getPlayerById(Message.playerId), true);
        Client.GetParty().sendToField(new PartyMemberEjectedMessage(Client.GetParty().ID, Message.playerId, Client.Character.ID));

    }

    @HandlerAttribute(ID = PartyLeaveRequestMessage.M_ID)
    public static void HandlePartyLeaveRequestMessage(WorldClient Client, PartyLeaveRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.GROUP)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        try {
            Client.EndGameAction(GameActionTypeEnum.GROUP);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = 5580)
    public static void HandlePartyAcceptInvitationMessage(WorldClient Client, PartyAcceptInvitationMessage Message) {
        if (Client.IsGameAction(GameActionTypeEnum.GROUP)) {
            PlayerController.SendServerMessage(Client, "Vous faites déjà partie d'un groupe ...");
            return;
        }
        PartyRequest GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId);
        } catch (Exception e) {
        }
        if (GameParty == null || !GameParty.Accept()) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

    }

    @HandlerAttribute(ID = 6254)
    public static void HandlePartyCancelInvitationMessage(WorldClient Client, PartyCancelInvitationMessage Message) {
        PartyRequest Req = Client.getPartyRequest(Message.partyId, Message.guestId);
        if (Req != null) {
            Req.Abort();
            Client.removePartyRequest(Req);
        } else if (Client.GetParty() != null && Client.GetParty().isChief(Client.Character)) {
            Client.GetParty().AbortRequest(Client, Message.guestId);
        } else {
            Client.Send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = PartyRefuseInvitationMessage.M_ID)
    public static void HandlePartyRefuseInvitationMessage(WorldClient Client, PartyRefuseInvitationMessage Message) {
        Party GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId).Requester.GetParty();
        } catch (Exception e) {
        }
        if (GameParty == null) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        Client.getPartyRequest(Message.partyId).Declin();
    }

    @HandlerAttribute(ID = PartyInvitationDetailsRequestMessage.M_ID)
    public static void HandlePartyInvitationDetailsRequestMessage(WorldClient Client, PartyInvitationDetailsRequestMessage Message) {
        Party GameParty = null;
        try {
            GameParty = Client.getPartyRequest(Message.partyId).Requester.GetParty();
        } catch (Exception e) {
        }
        if (GameParty == null) {
            return;
        }
        Client.Send(new PartyInvitationDetailsMessage(GameParty.ID, GameParty.Type, GameParty.PartyName, Client.getPartyRequest(Message.partyId).Requester.Character.ID, Client.getPartyRequest(Message.partyId).Requester.Character.NickName, GameParty.Chief.ID, GameParty.toPartyInvitationMemberInformations(), GameParty.toPartyGuestInformations()));
    }

    @HandlerAttribute(ID = 5585)
    public static void HandlePartyInvitationRequestMessage(WorldClient Client, Message Message) {

        Player Target = PlayerDAO.GetCharacter(((PartyInvitationRequestMessage) Message).name);
        if (Target == null) {
            Client.Send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_NOT_FOUND));
            return;
        }
        if (Target.Client == null || Target.Status == PlayerStatusEnum.PLAYER_STATUS_AFK) {
            Client.Send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        if (!Target.Client.CanGameAction(GameActionTypeEnum.GROUP)) {
            Client.Send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (!Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (!Target.Client.CanGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.Send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_ALREADY_INVITED));
            return;
        }

        if (Client.GetParty() != null && Client.GetParty().isFull()) {
            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 302, new String[0]));
            Client.Send(new PartyCannotJoinErrorMessage(((GameParty) Client.GetGameAction(GameActionTypeEnum.GROUP)).Party.ID, PartyJoinErrorEnum.PARTY_JOIN_ERROR_NOT_ENOUGH_ROOM));
            return;
        }
        if (Target.Account.Data.Ignore(Client.getAccount().ID)) {
            Client.Send(new PartyCannotJoinErrorMessage(0, PartyJoinErrorEnum.PARTY_JOIN_ERROR_PLAYER_BUSY));
            return;
        }

        PartyRequest Request = new PartyRequest(Client, Target.Client);

        Client.addPartyRequest(Request);
        Target.Client.addPartyRequest(Request);

        if (Client.GetParty() == null) {
            new Party(Client.Character, Target);
        } else {
            if (Client.GetParty().Restricted && !Client.GetParty().isChief(Client.Character)) {
                PlayerController.SendServerMessage(Client, "Impossible d'inviter ce joueur, le groupe ne peut pas être modifié actuellement.");
                return;
            } else {
                Client.GetParty().addGuest(Target);
            }
        }

        Target.Send(new PartyInvitationMessage(Client.GetParty().ID, PartyTypeEnum.PARTY_TYPE_CLASSICAL, Client.GetParty().PartyName, Party.MaxParticipants, Client.Character.ID, Client.Character.NickName, Target.ID));

    }

}
