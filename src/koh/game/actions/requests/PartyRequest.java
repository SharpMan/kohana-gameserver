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

    public PartyRequest(WorldClient requester, WorldClient requested) {
        super(requester, requested);
    }

    @Override
    public boolean accept() {
        if (this.requester == null || this.requester.getParty() == null) {
            this.requested.removePartyRequest(this);
            this.requester.removePartyRequest(this);
            return false;
        }
        
        this.requester.getParty().addPlayer(this.requested.getCharacter());

        this.requested.removePartyRequest(this);
        this.requester.removePartyRequest(this);

        return super.accept();
    }

    public void abort() {
        if (!super.declin()) {
            return;
        }
        try {
            this.requester.getParty().sendToField(new PartyCancelInvitationNotificationMessage(this.requester.getParty().id, this.requester.getCharacter().getID(), this.requested.getCharacter().getID()));
            this.requested.send(new PartyInvitationCancelledForGuestMessage(this.requester.getParty().id, this.requester.getCharacter().getID()));
            this.requester.getParty().removeGuest(this.requested.getCharacter());
        }
        catch (NullPointerException e1){
            this.requested.send(new PartyInvitationCancelledForGuestMessage(this.requester.getParty().id, this.requester.getCharacter().getID()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.requested.removePartyRequest(this);
    }

    @Override
    public boolean declin() {
        if (!super.declin()) {
            return false;
        }
        try {
            this.requested.send(new PartyInvitationCancelledForGuestMessage(this.requester.getParty().id, this.requested.getCharacter().getID()));

            this.requester.getParty().sendToField(new PartyRefuseInvitationNotificationMessage(this.requester.getParty().id, this.requested.getCharacter().getID()));

            this.requester.getParty().removeGuest(this.requested.getCharacter());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.requester.removePartyRequest(this);
        this.requested.removePartyRequest(this);

        return true;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum action) {
        return false;
    }

}
