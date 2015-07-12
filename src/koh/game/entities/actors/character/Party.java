package koh.game.entities.actors.character;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.PartyRequest;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.network.ChatChannel;
import koh.game.network.WorldClient;
import static koh.protocol.MessageEnum.PartyMemberRemoveMessage;
import koh.protocol.client.enums.PartyTypeEnum;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.context.roleplay.party.PartyDeletedMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyFollowStatusUpdateMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyJoinMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyKickedByMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyLeaderUpdateMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyLeaveMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyMemberRemoveMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyNewGuestMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyNewMemberMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyUpdateMessage;
import koh.protocol.types.game.context.roleplay.party.PartyCompanionBaseInformations;
import koh.protocol.types.game.context.roleplay.party.PartyCompanionMemberInformations;
import koh.protocol.types.game.context.roleplay.party.PartyGuestInformations;
import koh.protocol.types.game.context.roleplay.party.PartyInvitationMemberInformations;
import koh.protocol.types.game.context.roleplay.party.PartyMemberInformations;

/**
 *
 * @author Neo-Craft
 */
public class Party extends IWorldEventObserver {

    public static volatile int NextID = 0;
    public static final byte MaxParticipants = 8;
    public final byte Type = PartyTypeEnum.PARTY_TYPE_CLASSICAL;

    public boolean Restricted;
    public volatile ChatChannel ChatChannel = new ChatChannel(); //CHANNEL_PARTY
    private CopyOnWriteArrayList<Player> Players = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Player> Guests = new CopyOnWriteArrayList<>();

    public final int ID;
    public String PartyName = "";

    public Player Chief;

    public Party(Player character, Player p2) {
        this.ID = NextID++;
        Chief = character;
        Guests.add(p2);
        this.addPlayer(character);
    }
    
    public boolean containsPlayer(Player p){
        return Players.contains(p);
    }

    public void removeGuest(Player character) {
        this.Guests.remove(character);
        if (this.MemberCounts() <= 1) {
            this.Clear();
        } else {
            //REFRESH GUEST
        }
    }

    public Player getPlayerById(int id) {
        try {
            return this.Players.stream().filter(x -> x.ID == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public int MemberCounts() {
        return this.Guests.size() + this.Players.size();
    }

    public void addPlayer(Player character) {
        character.Client.AddGameAction(new GameParty(character, this));
        this.Players.add(character);
        this.Guests.remove(character);
        this.sendToField(new PartyNewMemberMessage(this.ID, toMemberInformations(character)));
        //this.UpdateMember(character);
        this.registerPlayer(character);
        character.Send(new PartyJoinMessage(this.ID, Type, this.Chief.ID, MaxParticipants, toMemberInformations(), toPartyGuestInformations(), this.Restricted, this.PartyName));
    }

    public void UpdateMember(Player character) {
        if (this.Players.contains(character)) {
            this.sendToField(new PartyUpdateMessage(this.ID, toMemberInformations(character)));
        }
    }

    public PartyMemberInformations toMemberInformations(Player x) {
        return new PartyMemberInformations(x.ID, (byte) x.Level, x.NickName, x.GetEntityLook(), x.Breed, x.Sexe == 1, x.Life, x.MaxLife(), x.Prospection(), x.RegenRate, x.Initiative(false), x.AlignmentSide.value, x.CurrentMap.Position.posX, x.CurrentMap.Position.posY, x.CurrentMap.Id, x.CurrentMap.SubAreaId, new PlayerStatus(x.Status.value()), new PartyCompanionMemberInformations[0]);
    }

    public PartyMemberInformations[] toMemberInformations() {
        return this.Players.stream().map(x -> new PartyMemberInformations(x.ID, (byte) x.Level, x.NickName, x.GetEntityLook(), x.Breed, x.Sexe == 1, x.Life, x.MaxLife(), x.Prospection(), x.RegenRate, x.Initiative(false), x.AlignmentSide.value, x.CurrentMap.Position.posX, x.CurrentMap.Position.posY, x.CurrentMap.Id, x.CurrentMap.SubAreaId, new PlayerStatus(x.Status.value()), new PartyCompanionMemberInformations[0])).toArray(PartyMemberInformations[]::new);
    }

    public PartyInvitationMemberInformations[] toPartyInvitationMemberInformations() {
        return this.Players.stream().map(x -> new PartyInvitationMemberInformations(x.ID, (byte) x.Level, x.NickName, x.GetEntityLook(), x.Breed, x.Sexe == 1, x.CurrentMap.Position.posX, x.CurrentMap.Position.posY, x.CurrentMap.Id, x.CurrentMap.SubAreaId, new PartyCompanionBaseInformations[0])).toArray(PartyInvitationMemberInformations[]::new);
    }

    public void addGuest(Player p) {
        if (this.Guests.addIfAbsent(p)) {
            this.sendToField(new PartyNewGuestMessage(this.ID, this.toPartyGuestInformations(p)));
        }
    }

    public PartyGuestInformations[] toPartyGuestInformations() {
        return this.Guests.stream().map(player -> new PartyGuestInformations(player.ID, this.Chief.ID, player.NickName, player.GetEntityLook(), player.Breed, player.Sexe == 1, player.Status, new PartyCompanionBaseInformations[0])).toArray(PartyGuestInformations[]::new);
    }

    public boolean isFull() {
        return Players.size() >= MaxParticipants;
    }

    public void Leave(Player player, boolean kicked) {
        if (player == null || Players == null) {
            return;
        }
        if (!Players.contains(player)) {
            return;
        }
        if (kicked) {
            player.Send(new PartyKickedByMessage(this.ID, this.Chief.ID));
            player.Client.DelGameAction(GameActionTypeEnum.GROUP);
        }
        if (player.Followers != null) { //int partyId, boolean success, int followedId
            player.Followers.forEach(x -> {
                x.Send(new PartyFollowStatusUpdateMessage(this.ID, true, player.ID));
            });
            player.Followers.clear();
        }
        this.unregisterPlayer(player);
        Players.remove(player);
        if (!kicked) {
            this.sendToField(new PartyMemberRemoveMessage(this.ID, player.ID));
        }
        if (this.MemberCounts() <= 1) {
            Clear();
        } else {
            if (this.isChief(player)) {
                this.UpdateLeader(null);
            }
        }

    }

    public synchronized void UpdateLeader(Player p) {
        this.Chief = p == null ? this.Players.get(0) : p;
        this.sendToField(new PartyLeaderUpdateMessage(this.ID, Chief.ID));
    }

    public void AbortRequest(WorldClient Client, int guestId) {
        //TODO: Send ChiefName if isn't the owner of invitation
        try {
            WorldClient WC = this.Players.stream().filter(x -> x.Client.getPartyRequest(this.ID, guestId) != null).findFirst().get().Client;
            PartyRequest Req = WC.getPartyRequest(this.ID, guestId);
            Req.Abort();
            WC.removePartyRequest(Req);
        } catch (Exception e) {
        }
    }

    public boolean isChief(int id) {
        return this.Chief != null && this.Chief.ID == id;
    }

    public boolean isChief(Player perso) {
        return this.Chief != null && perso != null && perso.ID == this.Chief.ID;
    }

    public PartyGuestInformations toPartyGuestInformations(Player player) {
        return new PartyGuestInformations(player.ID, this.Chief.ID, player.NickName, player.GetEntityLook(), player.Breed, player.Sexe == 1, player.Status, new PartyCompanionBaseInformations[0]);
    }

    public void Clear() {
        try {
            for (Player p : this.Players) {
                p.Client.EndGameAction(GameActionTypeEnum.GROUP);
            }
            this.sendToField(new PartyDeletedMessage(this.ID));
            this.Chief = null;
            this.Guests.clear();
            this.Guests = null;
            this.Players.clear();
            this.Players = null;
            this.finalize();
        } catch (Throwable e) {

        }

    }

    public void FollowAll(Player playerById) {
        if (playerById != null) {
            this.Players.stream().filter(x -> x.ID != playerById.ID).forEach(x -> {
                playerById.addFollower(x);
            });
        }
    }

    public void UnFollowAll(Player playerById) {
        if (playerById != null && playerById.Followers != null) {
            playerById.Followers.forEach(x -> {
                x.Send(new PartyFollowStatusUpdateMessage(this.ID, false, playerById.ID));
            });
            playerById.Followers.clear();
        }
    }

}
