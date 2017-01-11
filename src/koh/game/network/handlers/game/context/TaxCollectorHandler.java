package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameTaxCollectorDefenderAction;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.fights.Fight;
import koh.game.fights.types.TaxCollectorFight;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.FighterRefusedReasonEnum;
import koh.protocol.client.enums.TaxCollectorStateEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.guild.ChallengeFightJoinRefusedMessage;
import koh.protocol.messages.game.guild.tax.*;

/**
 * Created by Melancholia on 12/9/16.
 */
public class TaxCollectorHandler {

    @HandlerAttribute(ID = GuildFightJoinRequestMessage.M_ID)
    public static void handleGuildFightJoinRequestMessage(WorldClient client, GuildFightJoinRequestMessage msg){
        if(!client.canGameAction(GameActionTypeEnum.FIGHT)){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
            return;
        }
        final TaxCollector taxCollector = client.getCharacter().getGuild().getTaxCollector(msg.taxCollectorId);
        if(taxCollector == null || taxCollector.getCurrent_fight() == null || client.isGameAction(GameActionTypeEnum.DEFEND_TAX_COLLECTOR)){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_OCCUPIED));
        }
        else if(taxCollector.getState() != TaxCollectorStateEnum.STATE_WAITING_FOR_HELP){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.TOO_LATE));
        }
        else if(taxCollector.getCurrent_fight().isFull(taxCollector.getCurrent_fight().getTeam2())){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.TEAM_FULL));
        }
        else {
            client.addGameAction(new GameTaxCollectorDefenderAction(client.getCharacter(), taxCollector));
            taxCollector.getCurrent_fight().getDefenders().addIfAbsent(client.getCharacter());
            for (Player player : taxCollector.getCurrent_fight().getDefenders()) {
                player.send(new GuildFightPlayersHelpersJoinMessage(msg.taxCollectorId, client.getCharacter().toCharacterMinimalPlusLookInformation()));
            }
         }
    }

    @HandlerAttribute(ID = GuildFightLeaveRequestMessage.M_ID)
    public static void handleGuildFightLeaveRequestMessage(WorldClient client, GuildFightLeaveRequestMessage msg){
        final TaxCollector taxCollector = client.getCharacter().getGuild().getTaxCollector(msg.taxCollectorId);
        if(taxCollector == null || taxCollector.currentState() != 1 || !client.isGameAction(GameActionTypeEnum.DEFEND_TAX_COLLECTOR)){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.INSUFFICIENT_RIGHTS));
        }else{
            for (Player player : taxCollector.getCurrent_fight().getDefenders()) {
                player.send(new GuildFightPlayersHelpersLeaveMessage(msg.taxCollectorId, client.getCharacter().getID()));
            }
            taxCollector.getCurrent_fight().getDefenders().remove(client.getCharacter());
            client.endGameAction(GameActionTypeEnum.DEFEND_TAX_COLLECTOR);
        }
    }


    @HandlerAttribute(ID = GameRolePlayTaxCollectorFightRequestMessage.M_ID)
    public static void handleGameRolePlayTaxCollectorFightRequestMessage(WorldClient client,GameRolePlayTaxCollectorFightRequestMessage msg){
        if(!client.canGameAction(GameActionTypeEnum.FIGHT) || !client.getCharacter().getCurrentMap().getMyGameActors().containsKey(msg.taxCollectorId)){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_NOT_MEMBER));
            return;
        }
        final TaxCollector taxCollector = (TaxCollector) client.getCharacter().getCurrentMap().getActor(msg.taxCollectorId);
        if(taxCollector.getCurrent_fight() != null){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.OPPONENT_OCCUPIED));
        }
        else if(client.getCharacter().getGuild() == taxCollector.getGuild()){
            client.send(new ChallengeFightJoinRefusedMessage(client.getCharacter().getID(), FighterRefusedReasonEnum.WRONG_GUILD));
        }else {
            client.abortGameActions();
            final TaxCollectorFight fight = new TaxCollectorFight(client.getCharacter().getCurrentMap(), client, taxCollector);
            taxCollector.setCurrent_fight(fight);
            taxCollector.setState(TaxCollectorStateEnum.STATE_WAITING_FOR_HELP);
            client.getCharacter().getCurrentMap().destroyActor(taxCollector);
            client.getCharacter().getCurrentMap().addFight(fight);
            taxCollector.getGuild().sendToField(new TaxCollectorAttackedMessage(taxCollector.getFirstName(),
                    taxCollector.getLastName(),
                    taxCollector.getDofusMap().coordinates().worldX,
                    taxCollector.getDofusMap().coordinates().worldY,
                    taxCollector.getDofusMap().getId(),
                    taxCollector.getDofusMap().getSubAreaId(),
                    taxCollector.getGuild().getBasicGuildInformations()
            ));
        }

    }

}
