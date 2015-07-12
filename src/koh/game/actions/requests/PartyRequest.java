package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.roleplay.party.PartyCancelInvitationNotificationMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyInvitationCancelledForGuestMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyRefuseInvitationNotificationMessage;

/**
 *
 * @author Neo-Craft
 */
public class PartyRequest extends GameBaseRequest {

    public PartyRequest(WorldClient Requester, WorldClient Requested) {
        super(Requester, Requested);
    }

    @Override
    public boolean Accept() {
        if (this.Requester == null || this.Requester.GetParty() == null) {
            this.Requested.removePartyRequest(this);
            this.Requester.removePartyRequest(this);
            return false;
        }
        
        this.Requester.GetParty().addPlayer(this.Requested.Character);

        this.Requested.removePartyRequest(this);
        this.Requester.removePartyRequest(this);

        return super.Accept();
    }

    public void Abort() {
        if (!super.Declin()) {
            return;
        }
        try {
            this.Requester.GetParty().sendToField(new PartyCancelInvitationNotificationMessage(this.Requester.GetParty().ID, this.Requester.Character.ID, this.Requested.Character.ID));
            this.Requested.Send(new PartyInvitationCancelledForGuestMessage(this.Requester.GetParty().ID, this.Requester.Character.ID));
            this.Requester.GetParty().removeGuest(this.Requested.Character);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.Requested.removePartyRequest(this);
    }

    @Override
    public boolean Declin() {
        if (!super.Declin()) {
            return false;
        }
        try {
            this.Requested.Send(new PartyInvitationCancelledForGuestMessage(this.Requester.GetParty().ID, this.Requested.Character.ID));

            this.Requester.GetParty().sendToField(new PartyRefuseInvitationNotificationMessage(this.Requester.GetParty().ID, this.Requested.Character.ID));

            this.Requester.GetParty().removeGuest(this.Requested.Character);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.Requester.removePartyRequest(this);
        this.Requested.removePartyRequest(this);

        return true;
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum Action) {
        return false;
    }

}
