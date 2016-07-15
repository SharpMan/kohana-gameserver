package koh.game.network.handlers.game.context.roleplay;

import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameKolissium;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.PlayerInst;
import koh.game.entities.actors.character.ScoreType;
import koh.game.entities.kolissium.ArenaBattle;
import koh.game.entities.kolissium.ArenaParty;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.network.handlers.character.PartyHandler;
import koh.protocol.client.enums.PvpArenaStepEnum;
import koh.protocol.client.enums.PvpArenaTypeEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.roleplay.fight.arena.*;
import koh.protocol.messages.game.context.roleplay.party.PartyInvitationArenaRequestMessage;

import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author Neo-Craft
 */
public class ArenaHandler {

    @HandlerAttribute(ID = GameRolePlayArenaRegisterMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaRegisterMessage(WorldClient client, GameRolePlayArenaRegisterMessage message) {
        if (client.getCharacter().getLevel() < 50) {
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 326));
        } else if (PlayerInst.isPresent(client.getCharacter().getID()) && PlayerInst.getPlayerInst(client.getCharacter().getID()).getBannedTime() > System.currentTimeMillis()) {
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 343, TimeUnit.MILLISECONDS.toMinutes(Instant.ofEpochMilli(PlayerInst.getPlayerInst(client.getCharacter().getID()).getBannedTime()).minusMillis(System.currentTimeMillis()).toEpochMilli()) + ""));
        } else if (client.canGameAction(GameActionTypeEnum.KOLI)) {
            if(Main.DAY_OF_WEEK == Calendar.FRIDAY){
                PlayerController.sendServerMessage(client,"Exceptionnel : Durant le vendredi, le Kolizeum est en mode 2vs2");
            }
           if (client.getParty() instanceof ArenaParty && client.getParty().memberCounts() > 1) {
               if (client.getParty().getPlayers().stream().anyMatch(pl -> pl.getLevel() < 50)) {
                   client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 327, client.getParty().getPlayers().stream().filter(pl -> pl.getLevel() < 50).findFirst().get().getNickName()));
                   return;
               }else if (client.getParty().getPlayers().stream().map(Player::getBreed).distinct().count() != client.getParty().getPlayers().size()){
                   PlayerController.sendServerMessage(client,"1 classe unique par team");
                   return;
               }else if(client.getParty().asArena().inKolizeum()|| WorldServer.getKoli().groupIsRegistred(client.getParty().asArena())){
                   PlayerController.sendServerMessage(client,"Groupe déjà inscrit");
                   return;
               }
               client.getParty().getPlayers()
                        .stream()
                        .filter(pl -> WorldServer.getKoli().isRegistred(pl))
                        .forEach(WorldServer.getKoli()::unregisterPlayer);
               try{
                   client.getParty().getPlayers().stream()
                           .filter(p -> p.getClient() == null)
                           .forEach(p -> client.getParty().leave(p,true));
               }catch (Exception e){
                   e.printStackTrace();
               }

                if (WorldServer.getKoli().registerGroup(client.getCharacter(), (ArenaParty) client.getParty())) {
                    client.getParty().getPlayers().forEach(pl -> {
                        pl.getClient().addGameAction(new GameKolissium(pl, (ArenaParty) client.getParty()));
                    });
                }
            } else {
               if(WorldServer.getKoli().isRegistred(client.getCharacter())){
                   PlayerController.sendServerMessage(client,"Vous êtes déjà inscrit");
                   return;
               }
                WorldServer.getKoli().registerPlayer(client.getCharacter());
                client.addGameAction(new GameKolissium(client.getCharacter()));
            }
            client.send(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_REGISTRED, PvpArenaTypeEnum.ARENA_TYPE_3VS3));


        } else {
            client.getGameActionCount();
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 217));
        }


    }

    @HandlerAttribute(ID = GameRolePlayArenaUnregisterMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaUnregisterMessage(WorldClient client, GameRolePlayArenaUnregisterMessage message) {
        client.endGameAction(GameActionTypeEnum.KOLI);
        //WorldServer.getKoli().unregisterPlayer(client.getCharacter());
        if (client.getParty() instanceof ArenaParty && client.getParty().memberCounts() > 1) {
            WorldServer.getKoli().unregisterGroup(client.getCharacter(),client.getParty().asArena());
        }
        client.send(new GameRolePlayArenaRegistrationStatusMessage(false,  PvpArenaStepEnum.ARENA_STEP_UNREGISTER, PvpArenaTypeEnum.ARENA_TYPE_3VS3));

    }

    @HandlerAttribute(ID = GameRolePlayArenaFightAnswerMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaFightAnswerMessage(WorldClient client, GameRolePlayArenaFightAnswerMessage message) {
        final ArenaBattle battle = DAO.getArenas().find(message.fightId);
        if (battle == null) {
            return;
        }
        if (message.accept) {
            battle.accept(client.getCharacter());
        } else {
            //FIXME sniffer?
        }
    }

    @HandlerAttribute(ID = 6301)
    public static void handleGameRolePlayArenaUpdatePlayerInfosMessage(WorldClient client, GameRolePlayArenaUpdatePlayerInfosMessage Message) {
        final PlayerInst inst = PlayerInst.getPlayerInst(client.getCharacter().getID());
        client.getCharacter().send(new GameRolePlayArenaUpdatePlayerInfosMessage(client.getCharacter().getKolizeumRate().getScreenRating(), inst.getDailyCote(), client.getCharacter().getScores().get(ScoreType.BEST_COTE), inst.getDailyWins(), inst.getDailyFight()));

    }

    @HandlerAttribute(ID = PartyInvitationArenaRequestMessage.M_ID)
    public static void handlePartyInvitationArenaRequestMessage(WorldClient client, PartyInvitationArenaRequestMessage message) {
        PartyHandler.handlePartyInvitationRequestMessage(client, message);
    }

}
