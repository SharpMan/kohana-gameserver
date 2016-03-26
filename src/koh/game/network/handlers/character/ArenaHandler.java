package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameKolissium;
import koh.game.controllers.PlayerController;
import koh.game.entities.kolissium.ArenaParty;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.KolizeoEnum;
import koh.protocol.client.enums.PvpArenaStepEnum;
import koh.protocol.client.enums.PvpArenaTypeEnum;
import koh.protocol.messages.game.context.roleplay.fight.arena.*;
import koh.protocol.messages.game.context.roleplay.party.PartyInvitationArenaRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class ArenaHandler {

    @HandlerAttribute(ID = GameRolePlayArenaRegisterMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaRegisterMessage(WorldClient client,GameRolePlayArenaRegisterMessage message){
        if(client.getCharacter().getLevel() < 50){
            PlayerController.sendServerMessage(client, "Vous devez être au moins niveau 50 pour faire des combats en Kolizéum.");
        }
        if(client.canGameAction(GameActionTypeEnum.KOLI)){
           if(client.getParty() instanceof ArenaParty){
               client.getParty().getPlayers()
                       .stream()
                       .filter(pl -> WorldServer.getKoli().isRegistred(pl))
                       .forEach(WorldServer.getKoli()::unregisterPlayer);

               if(WorldServer.getKoli().registerGroup(client.getCharacter(), (ArenaParty) client.getParty())){
                   client.getParty().getPlayers().forEach(pl -> {
                       pl.getClient().addGameAction(new GameKolissium(pl, (ArenaParty) client.getParty()));
                   });
               }
           }else{
               WorldServer.getKoli().registerPlayer(client.getCharacter());
           }

        }else{
            PlayerController.sendServerMessage(client, "Vous êtes occupés.");
        }
        client.send(new GameRolePlayArenaRegistrationStatusMessage(true, PvpArenaStepEnum.ARENA_STEP_REGISTRED, PvpArenaTypeEnum.ARENA_TYPE_3VS3));


        //client.send(new GameRolePlayArenaFightPropositionMessage(new int[]{1,2,3} , KolizeoEnum.DURATION, 1400));
    }

    @HandlerAttribute(ID = GameRolePlayArenaUnregisterMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaUnregisterMessage(WorldClient client, GameRolePlayArenaUnregisterMessage message){
        client.send(new GameRolePlayArenaRegistrationStatusMessage(false, (byte) 0, 3));
        //todo
    }

    @HandlerAttribute(ID = GameRolePlayArenaFightAnswerMessage.MESSAGE_ID)
    public static void handleGameRolePlayArenaFightAnswerMessage(WorldClient client, GameRolePlayArenaFightAnswerMessage message){

    }
    
    @HandlerAttribute(ID = 6301)
    public static void handleGameRolePlayArenaUpdatePlayerInfosMessage(WorldClient Client , GameRolePlayArenaUpdatePlayerInfosMessage Message){
        PlayerController.sendServerMessage(Client, "Option non disponnible");
    }

    @HandlerAttribute(ID = PartyInvitationArenaRequestMessage.M_ID)
    public static void handlePartyInvitationArenaRequestMessage(WorldClient client, PartyInvitationArenaRequestMessage message) {
        PartyHandler.handlePartyInvitationRequestMessage(client,message);
    }

}
