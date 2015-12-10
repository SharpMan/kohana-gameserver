package koh.game.entities.actors.character;

import java.util.concurrent.CopyOnWriteArrayList;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameParty;
import koh.game.actions.requests.PartyRequest;
import koh.game.entities.actors.Player;
import koh.game.entities.environments.IWorldEventObserver;
import koh.game.network.ChatChannel;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.PartyTypeEnum;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.context.roleplay.party.PartyDeletedMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyFollowStatusUpdateMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyJoinMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyKickedByMessage;
import koh.protocol.messages.game.context.roleplay.party.PartyLeaderUpdateMessage;
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

    public static volatile int nextID = 0;
    public static final byte MAX_PARTICIPANTS = 8;
    public final byte type = PartyTypeEnum.PARTY_TYPE_CLASSICAL;

    public boolean restricted;
    public volatile ChatChannel chatChannel = new ChatChannel(); //CHANNEL_PARTY
    private CopyOnWriteArrayList<Player> players = new CopyOnWriteArrayList<>();
    public CopyOnWriteArrayList<Player> guests = new CopyOnWriteArrayList<>();

    public final int id;
    public String partyName = "";

    public Player chief;

    public Party(Player character, Player p2) {
        this.id = nextID++;
        chief = character;
        guests.add(p2);
        this.addPlayer(character);
    }
    
    public boolean containsPlayer(Player p){
        return players.contains(p);
    }

    public void removeGuest(Player character) {
        this.guests.remove(character);
        if (this.memberCounts() <= 1) {
            this.clear();
        } else {
            //REFRESH GUEST
        }
    }

    public Player getPlayerById(int id) {
        try {
            return this.players.stream().filter(x -> x.ID == id).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public int memberCounts() {
        return this.guests.size() + this.players.size();
    }

    public void addPlayer(Player character) {
        character.client.addGameAction(new GameParty(character, this));
        this.players.add(character);
        this.guests.remove(character);
        this.sendToField(new PartyNewMemberMessage(this.id, toMemberInformations(character)));
        //this.updateMember(character);
        this.registerPlayer(character);
        character.send(new PartyJoinMessage(this.id, type, this.chief.ID, MAX_PARTICIPANTS, toMemberInformations(), toPartyGuestInformations(), this.restricted, this.partyName));
    }

    public void updateMember(Player character) {
        if (this.players.contains(character)) {
            this.sendToField(new PartyUpdateMessage(this.id, toMemberInformations(character)));
        }
    }

    public PartyMemberInformations toMemberInformations(Player x) {
        return new PartyMemberInformations(x.ID, (byte) x.level, x.nickName, x.getEntityLook(), x.breed, x.sexe == 1, x.life, x.getMaxLife(), x.getProspection(), x.regenRate, x.getInitiative(false), x.alignmentSide.value, x.currentMap.getPosition().getPosX(), x.currentMap.getPosition().getPosY(), x.currentMap.getId(), x.currentMap.getSubAreaId(), new PlayerStatus(x.status.value()), new PartyCompanionMemberInformations[0]);
    }

    public PartyMemberInformations[] toMemberInformations() {
        return this.players.stream().map(x -> new PartyMemberInformations(x.ID, (byte) x.level, x.nickName, x.getEntityLook(), x.breed, x.sexe == 1, x.life, x.getMaxLife(), x.getProspection(), x.regenRate, x.getInitiative(false), x.alignmentSide.value, x.currentMap.getPosition().getPosX(), x.currentMap.getPosition().getPosY(), x.currentMap.getId(), x.currentMap.getSubAreaId(), new PlayerStatus(x.status.value()), new PartyCompanionMemberInformations[0])).toArray(PartyMemberInformations[]::new);
    }

    public PartyInvitationMemberInformations[] toPartyInvitationMemberInformations() {
        return this.players.stream().map(x -> new PartyInvitationMemberInformations(x.ID, (byte) x.level, x.nickName, x.getEntityLook(), x.breed, x.sexe == 1, x.currentMap.getPosition().getPosX(), x.currentMap.getPosition().getPosY(), x.currentMap.getId(), x.currentMap.getSubAreaId(), new PartyCompanionBaseInformations[0])).toArray(PartyInvitationMemberInformations[]::new);
    }

    public void addGuest(Player p) {
        if (this.guests.addIfAbsent(p)) {
            this.sendToField(new PartyNewGuestMessage(this.id, this.toPartyGuestInformations(p)));
        }
    }

    public PartyGuestInformations[] toPartyGuestInformations() {
        return this.guests.stream().map(player -> new PartyGuestInformations(player.ID, this.chief.ID, player.nickName, player.getEntityLook(), player.breed, player.sexe == 1, player.status, new PartyCompanionBaseInformations[0])).toArray(PartyGuestInformations[]::new);
    }

    public boolean isFull() {
        return players.size() >= MAX_PARTICIPANTS;
    }

    public void leave(Player player, boolean kicked) {
        if (player == null || players == null) {
            return;
        }
        if (!players.contains(player)) {
            return;
        }
        if (kicked) {
            player.send(new PartyKickedByMessage(this.id, this.chief.ID));
            player.client.delGameAction(GameActionTypeEnum.GROUP);
        }
        if (player.followers != null) { //int partyId, boolean success, int followedId
            player.followers.forEach(x -> {
                x.send(new PartyFollowStatusUpdateMessage(this.id, true, player.ID));
            });
            player.followers.clear();
        }
        this.unregisterPlayer(player);
        players.remove(player);
        if (!kicked) {
            this.sendToField(new PartyMemberRemoveMessage(this.id, player.ID));
        }
        if (this.memberCounts() <= 1) {
            clear();
        } else {
            if (this.isChief(player)) {
                this.updateLeader(null);
            }
        }

    }

    public synchronized void updateLeader(Player p) {
        this.chief = p == null ? this.players.get(0) : p;
        this.sendToField(new PartyLeaderUpdateMessage(this.id, chief.ID));
    }

    public void abortRequest(WorldClient client, int guestId) {
        //TODO: send ChiefName if isn't the owner of invitation
        try {
            WorldClient WC = this.players.stream().filter(x -> x.client.getPartyRequest(this.id, guestId) != null).findFirst().get().client;
            PartyRequest Req = WC.getPartyRequest(this.id, guestId);
            Req.Abort();
            WC.removePartyRequest(Req);
        } catch (Exception e) {
        }
    }

    public boolean isChief(int id) {
        return this.chief != null && this.chief.ID == id;
    }

    public boolean isChief(Player perso) {
        return this.chief != null && perso != null && perso.ID == this.chief.ID;
    }

    public PartyGuestInformations toPartyGuestInformations(Player player) {
        return new PartyGuestInformations(player.ID, this.chief.ID, player.nickName, player.getEntityLook(), player.breed, player.sexe == 1, player.status, new PartyCompanionBaseInformations[0]);
    }

    public void clear() {
        try {
            for (Player p : this.players) {
                p.client.endGameAction(GameActionTypeEnum.GROUP);
            }
            this.sendToField(new PartyDeletedMessage(this.id));
            this.chief = null;
            this.guests.clear();
            this.guests = null;
            this.players.clear();
            this.players = null;
            this.finalize();
        } catch (Throwable e) {

        }

    }

    public void followAll(Player playerById) {
        if (playerById != null) {
            this.players.stream().filter(x -> x.ID != playerById.ID).forEach(x -> {
                playerById.addFollower(x);
            });
        }
    }

    public void unFollowAll(Player playerById) {
        if (playerById != null && playerById.followers != null) {
            playerById.followers.forEach(x -> {
                x.send(new PartyFollowStatusUpdateMessage(this.id, false, playerById.ID));
            });
            playerById.followers.clear();
        }
    }

}
