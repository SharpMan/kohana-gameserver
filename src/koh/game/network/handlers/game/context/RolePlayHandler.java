package koh.game.network.handlers.game.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
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
import koh.protocol.messages.game.context.roleplay.stats.StatsUpgradeResultMessage;
import koh.protocol.types.game.context.roleplay.HumanOptionEmote;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class RolePlayHandler {

    //TODO ImmutableMap
    public static final Map<Integer, StatsEnum> BOOST_ID_TO_STATS = new HashMap<Integer, StatsEnum>(6) {
        {
            put(StatsBoostEnum.Strength, StatsEnum.Strength);
            put(StatsBoostEnum.Vitality, StatsEnum.Vitality);
            put(StatsBoostEnum.Wisdom, StatsEnum.Wisdom);
            put(StatsBoostEnum.Chance, StatsEnum.Chance);
            put(StatsBoostEnum.Agility, StatsEnum.Agility);
            put(StatsBoostEnum.Intelligence, StatsEnum.Intelligence);
        }
    };

    private static final Logger logger = LogManager.getLogger(RolePlayHandler.class);

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
                client.getCharacter().updateRegenedLife(true);
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
        StatsEnum Stat = BOOST_ID_TO_STATS.get(message.statId);
        if (Stat == null) {
            logger.error("Wrong statsid {}", message.statId);
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
        client.getCharacter().getStats().getEffect(Stat).Base = base;
        switch ((int) message.statId) {
            case StatsBoostEnum.Strength:
                client.getCharacter().setStrength(base);
                break;

            case StatsBoostEnum.Vitality:
                client.getCharacter().setVitality(base);
                client.getCharacter().addLife(base - oldbase); // on boost la life
                break;

            case StatsBoostEnum.Wisdom:
                client.getCharacter().setWisdom(base);
                break;

            case StatsBoostEnum.Intelligence:
                client.getCharacter().setIntell(base);
                break;

            case StatsBoostEnum.Chance:
                client.getCharacter().setChance(base);
                break;

            case StatsBoostEnum.Agility:
                client.getCharacter().setAgility(base);
                break;
        }
        client.getCharacter().addStatPoints(- (int) message.boostPoint - num1);
        client.send(new StatsUpgradeResultMessage(StatsUpgradeResultEnum.SUCCESS, message.boostPoint));
        client.getCharacter().refreshStats();
    }

    @HandlerAttribute(ID = ChangeMapMessage.MESSAGE_ID)
    public static void HandleChangeMapMessage(WorldClient Client, ChangeMapMessage Message) {

        if (Client.getCharacter().getCell() == null || !Client.getCharacter().getCell().affectMapChange()) {
            System.out.println("undefined cell");
            Client.send(new BasicNoOperationMessage());
            return;
        }
        //client.sequenceMessage();
        //client.sendPacket(new BasicNoOperationMessage());

        //System.out.println(cell.mapChangeData + "cell" + cell.id);
        if (Client.getCharacter().getCurrentMap().getTopNeighbourId() == Message.mapId) {
            Client.getCharacter().teleport(Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[0].getMapid() : Message.mapId, Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[0].getCellid() : (Client.getCharacter().getCell().getId() + 532));
        } else if (Client.getCharacter().getCurrentMap().getBottomNeighbourId() == Message.mapId) {
            Client.getCharacter().teleport(Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[1].getMapid() : Message.mapId, Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[1].getCellid() : (Client.getCharacter().getCell().getId() - 532));
        } else if (Client.getCharacter().getCurrentMap().getLeftNeighbourId() == Message.mapId) {
            Client.getCharacter().teleport(Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[2].getMapid() : Message.mapId, Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[2].getCellid() : (Client.getCharacter().getCell().getId() + 13));
        } else if (Client.getCharacter().getCurrentMap().getRightNeighbourId() == Message.mapId) {
            Client.getCharacter().teleport(Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[3].getMapid() : Message.mapId, Client.getCharacter().getCurrentMap().getNewNeighbour() != null ? Client.getCharacter().getCurrentMap().getNewNeighbour()[3].getCellid() : (Client.getCharacter().getCell().getId() - 13));
        } else {
            // Client.getCharacter().teleport(Message.mapId, -1);
            logger.error("client {} teleport from {} to {}" ,Client.getCharacter().getNickName(),Client.getCharacter().getCurrentMap().getId(), Message.mapId);
            Client.send(new BasicNoOperationMessage());
            //System.out.println("undefinied map");
        }

    }

}
