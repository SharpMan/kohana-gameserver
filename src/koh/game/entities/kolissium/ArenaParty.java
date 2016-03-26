package koh.game.entities.kolissium;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.game.network.WorldServer;
import koh.protocol.client.enums.PartyTypeEnum;
import koh.protocol.messages.game.character.status.PlayerStatus;
import koh.protocol.messages.game.chat.ChatServerMessage;
import koh.protocol.types.game.context.roleplay.party.PartyCompanionMemberInformations;
import koh.protocol.types.game.context.roleplay.party.PartyMemberArenaInformations;
import koh.protocol.types.game.context.roleplay.party.PartyMemberInformations;

import java.time.Instant;

import static koh.protocol.client.enums.ChatActivableChannelsEnum.CHANNEL_PARTY;

/**
 * Created by Melancholia on 3/20/16.
 */
public class ArenaParty extends Party {

    private boolean inKolizeum;

    public ArenaParty(Player character, Player p2) {
        super(character, p2, PartyTypeEnum.PARTY_TYPE_ARENA);
        partyName = "Kolizeum";
    }

    public ArenaParty(Player p1, Player p2, Player p3) {
        this(p1, p2);
        this.addPlayer(p2);
        this.addPlayer(p3);
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        if (this.inKolizeum) {
            this.sendMessageToGroup(
                    "Votre groupe a été désinscrit du Kolizeum car <b>" + player.getNickName() + "</b> vient de le rejoindre.");
            WorldServer.getKoli().unregisterGroupForced(this);
        }
    }

    @Override
    public void leave(Player player, boolean kicked) {
        if (this.inKolizeum) {
            this.sendMessageToGroup(
                    "Votre groupe a été désinscrit du Kolizeum car <b>" + player.getNickName() + "</b> l'a quitté.");
            WorldServer.getKoli().unregisterGroupForced(this);
        }
        super.leave(player,kicked);
    }

    @Override
    public PartyMemberInformations toMemberInformations(Player pl) {
        return new PartyMemberArenaInformations(pl.getID(), (byte) pl.getLevel(), pl.getNickName(), pl.getEntityLook(), pl.getBreed(), pl.hasSexe(), pl.getLife(), pl.getMaxLife(), pl.getProspection(), pl.getRegenRate(), pl.getInitiative(false), pl.getAlignmentSide().value, pl.getCurrentMap().getPosition().getPosX(), pl.getCurrentMap().getPosition().getPosY(), pl.getCurrentMap().getId(), pl.getCurrentMap().getSubAreaId(), new PlayerStatus(pl.getStatus().value()), new PartyCompanionMemberInformations[0], pl.getKolizeumRate().getRating());
    }

    @Override
    public PartyMemberInformations[] toMemberInformations() {
        return this.players.stream().map(pl -> new PartyMemberArenaInformations(pl.getID(), (byte) pl.getLevel(), pl.getNickName(), pl.getEntityLook(), pl.getBreed(), pl.hasSexe(), pl.getLife(), pl.getMaxLife(), pl.getProspection(), pl.getRegenRate(), pl.getInitiative(false), pl.getAlignmentSide().value, pl.getCurrentMap().getPosition().getPosX(), pl.getCurrentMap().getPosition().getPosY(), pl.getCurrentMap().getId(), pl.getCurrentMap().getSubAreaId(), new PlayerStatus(pl.getStatus().value()), new PartyCompanionMemberInformations[0], pl.getKolizeumRate().getRating())).toArray(PartyMemberInformations[]::new);
    }

    public synchronized void setInKolizeum(boolean is) {
        this.inKolizeum = is;
    }

    public boolean inKolizeum() {
        return inKolizeum;
    }

    public void sendMessageToGroup(String msg) {
        this.sendToField(new ChatServerMessage(CHANNEL_PARTY, msg, (int) Instant.now().getEpochSecond(), "az", -100, "[Kolissium]", 1));
    }
}
