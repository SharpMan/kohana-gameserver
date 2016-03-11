package koh.game.network.handlers.game.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.MonsterGroup;
import koh.game.fights.Fight;
import koh.game.fights.types.MonsterFight;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.StatsBoostEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.StatsUpgradeResultEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.connection.StatsUpgradeRequestMessage;
import koh.protocol.messages.game.character.stats.UpdateLifePointsMessage;
import koh.protocol.messages.game.context.roleplay.ChangeMapMessage;
import koh.protocol.messages.game.context.roleplay.emote.EmotePlayMessage;
import koh.protocol.messages.game.context.roleplay.emote.EmotePlayRequestMessage;
import koh.protocol.messages.game.context.roleplay.figh.GameRolePlayAttackMonsterRequestMessage;
import koh.protocol.messages.game.context.roleplay.stats.StatsUpgradeResultMessage;
import koh.protocol.types.game.context.roleplay.HumanOptionEmote;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
@Log4j2
public class RolePlayHandler {

    //TODO ImmutableMap
    public static final Map<Integer, StatsEnum> BOOST_ID_TO_STATS = new HashMap<Integer, StatsEnum>(6) {
        {
            put(StatsBoostEnum.STRENGTH, StatsEnum.STRENGTH);
            put(StatsBoostEnum.VITALITY, StatsEnum.VITALITY);
            put(StatsBoostEnum.WISDOM, StatsEnum.WISDOM);
            put(StatsBoostEnum.CHANCE, StatsEnum.CHANCE);
            put(StatsBoostEnum.AGILITY, StatsEnum.AGILITY);
            put(StatsBoostEnum.INTELLIGENCE, StatsEnum.INTELLIGENCE);
        }
    };

    @HandlerAttribute(ID = GameRolePlayAttackMonsterRequestMessage.M_ID)
    public static void handleGameRolePlayAttackMonsterRequestMessage(WorldClient client , GameRolePlayAttackMonsterRequestMessage message){
        if(client.isGameAction(GameActionTypeEnum.FIGHT) || !client.canGameAction(GameActionTypeEnum.FIGHT)){
            PlayerController.sendServerMessage(client,"Impossible : Vous êtes occupé(e)");
        }else {
            final IGameActor target = client.getCharacter().getCurrentMap().getActor(message.monsterGroupId);
            if(target == null || ! (target instanceof MonsterGroup)){
                client.send(new BasicNoOperationMessage());
                return;
            }
            client.abortGameActions();
            final Fight fight = new MonsterFight(client.getCharacter().getCurrentMap(), client,(MonsterGroup) target);
            client.getCharacter().getCurrentMap().addFight(fight);
        }

    }

    @HandlerAttribute(ID = EmotePlayRequestMessage.MESSAGE_ID)
    public static void EmotePlayRequestMessage(WorldClient client, EmotePlayRequestMessage message) {
        if(client.isGameAction(GameActionTypeEnum.FIGHT)){
            client.send(new BasicNoOperationMessage());
            return;
        }
        if(message.emoteId == 1 || message.emoteId == 19){
            if(client.getCharacter().getRegenRate() != 5) {
                client.getCharacter().stopRegen();
                client.getCharacter().setRegenRate((byte) 5);
                client.getCharacter().updateRegenedLife(false);
                client.send(new UpdateLifePointsMessage(client.getCharacter().getLife(),client.getCharacter().getMaxLife()));
            }
            client.getCharacter().removeHumanOption(HumanOptionEmote.class);
            client.getCharacter().getHumanInformations().options = ArrayUtils.add(client.getCharacter().getHumanInformations().options, new HumanOptionEmote(message.emoteId,Instant.now().toEpochMilli()));
         }else if(client.getCharacter().getRegenRate() == 5){
            client.getCharacter().removeHumanOption(HumanOptionEmote.class);
            client.getCharacter().stopRegen();
            client.getCharacter().updateRegenedLife(true);
            client.send(new UpdateLifePointsMessage(client.getCharacter().getLife(),client.getCharacter().getMaxLife()));
        }
        client.getCharacter().getCurrentMap().sendToField(new EmotePlayMessage(message.emoteId, Instant.now().getEpochSecond(), client.getCharacter().getID(), client.getAccount().id));
    }


    @HandlerAttribute(ID = StatsUpgradeRequestMessage.MESSAGE_ID)
    public static void HandleStatsUpgradeRequestMessage(WorldClient client, StatsUpgradeRequestMessage message) {
        if(client.isGameAction(GameActionTypeEnum.FIGHT)){
            PlayerController.sendServerMessage(client, "Tes statistiques ne seront affectés qu'aprés la fin du combat.");
        }
        if (message.useAdditionnal) {
            client.send(new BasicNoOperationMessage());
            PlayerController.sendServerMessage(client, "Not implanted yet");
            return;
        }
        StatsEnum Stat = BOOST_ID_TO_STATS.get((int)message.statId);
        if (Stat == null) {
            log.error("Wrong statsid {}", message.statId);
            return;
        }
        if (message.boostPoint <= 0) {
            throw new Error("client given 0 as boostpoint. Forbidden value.");
        }
        int base = client.getCharacter().getStats().getBase(Stat);
        short num1 = (short) message.boostPoint;
        if ((int) num1 < 1 || (int) message.boostPoint > client.getCharacter().getStatPoints()) {
            client.send(new BasicNoOperationMessage());
            return;
        }
        int oldbase = base;
        final List<List<Integer>> thresholds = DAO.getD2oTemplates().getBreed(client.getCharacter().getBreed()).GetThresholds((int) message.statId);
        for (int thresholdIndex = DAO.getD2oTemplates().getBreed(client.getCharacter().getBreed()).GetThresholdIndex((int) base, thresholds); (long) num1 >= (long) thresholds.get(thresholdIndex).get(1); thresholdIndex = DAO.getD2oTemplates().getBreed(client.getCharacter().getBreed()).GetThresholdIndex((int) base, thresholds)) {
            short num2;
            short num3;
            if (thresholdIndex < thresholds.size() - 1 && (double) num1 / (double) thresholds.get(thresholdIndex).get(1) > (double) ((long) thresholds.get(thresholdIndex + 1).get(0) - (long) base)) {
                num2 = (short) ((long) thresholds.get(thresholdIndex + 1).get(0) - (long) base);
                num3 = (short) ((long) num2 * (long) thresholds.get(thresholdIndex).get(1));
                if (thresholds.get(thresholdIndex).size() > 2) {
                    num2 = (short) ((long) num2 * (long) thresholds.get(thresholdIndex).get(2));
                }
            } else {
                num2 = (short) Math.floor((double) num1 / (double) thresholds.get(thresholdIndex).get(1));
                num3 = (short) ((long) num2 * (long) thresholds.get(thresholdIndex).get(1));
                if (thresholds.get(thresholdIndex).size() > 2) {
                    num2 = (short) ((long) num2 * (long) thresholds.get(thresholdIndex).get(2));
                }
            }
            base += num2;
            num1 -= num3;
        }
        client.getCharacter().getStats().getEffect(Stat).base = base;
        switch ((int) message.statId) {
            case StatsBoostEnum.STRENGTH:
                client.getCharacter().setStrength(base);
                break;

            case StatsBoostEnum.VITALITY:
                client.getCharacter().setVitality(base);
                client.getCharacter().healLife(base - oldbase); // on boost la life
                break;

            case StatsBoostEnum.WISDOM:
                client.getCharacter().setWisdom(base);
                break;

            case StatsBoostEnum.INTELLIGENCE:
                client.getCharacter().setIntell(base);
                break;

            case StatsBoostEnum.CHANCE:
                client.getCharacter().setChance(base);
                break;

            case StatsBoostEnum.AGILITY:
                client.getCharacter().setAgility(base);
                break;
        }
        client.getCharacter().addStatPoints(- (int) message.boostPoint - num1);
        client.send(new StatsUpgradeResultMessage(StatsUpgradeResultEnum.SUCCESS, message.boostPoint));
        client.getCharacter().refreshStats();
    }

    @HandlerAttribute(ID = ChangeMapMessage.MESSAGE_ID)
    public static void HandleChangeMapMessage(WorldClient client, ChangeMapMessage Message) {

        if (client.getCharacter().getCell() == null || !client.getCharacter().getCell().affectMapChange()) {
            log.error("undefined cell");
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.getCharacter().getCurrentMap().getTopNeighbourId() == Message.mapId) {
            client.getCharacter().teleport(client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[0].getMapid() : Message.mapId, client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[0].getCellid() : (client.getCharacter().getCell().getId() + 532));
        } else if (client.getCharacter().getCurrentMap().getBottomNeighbourId() == Message.mapId) {
            client.getCharacter().teleport(client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[1].getMapid() : Message.mapId, client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[1].getCellid() : (client.getCharacter().getCell().getId() - 532));
        } else if (client.getCharacter().getCurrentMap().getLeftNeighbourId() == Message.mapId) {
            client.getCharacter().teleport(client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[2].getMapid() : Message.mapId, client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[2].getCellid() : (client.getCharacter().getCell().getId() + 13));
        } else if (client.getCharacter().getCurrentMap().getRightNeighbourId() == Message.mapId) {
            client.getCharacter().teleport(client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[3].getMapid() : Message.mapId, client.getCharacter().getCurrentMap().getNewNeighbour() != null ? client.getCharacter().getCurrentMap().getNewNeighbour()[3].getCellid() : (client.getCharacter().getCell().getId() - 13));
        } else {
            // Client.getCharacter().teleport(Message.mapId, -1);
            log.error("client {} teleport from {} to {}" ,client.getCharacter().getNickName(),client.getCharacter().getCurrentMap().getId(), Message.mapId);
            client.send(new BasicNoOperationMessage());
            //System.out.println("undefinied map");
        }

    }

}
