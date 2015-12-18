package koh.game.network.handlers.game.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.StatsBoostEnum;
import koh.protocol.client.enums.StatsEnum;
import koh.protocol.client.enums.StatsUpgradeResultEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.connection.StatsUpgradeRequestMessage;
import koh.protocol.messages.game.context.roleplay.ChangeMapMessage;
import koh.protocol.messages.game.context.roleplay.emote.EmotePlayMessage;
import koh.protocol.messages.game.context.roleplay.emote.EmotePlayRequestMessage;
import koh.protocol.messages.game.context.roleplay.stats.StatsUpgradeResultMessage;
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
    public static void EmotePlayRequestMessage(WorldClient Client, EmotePlayRequestMessage Message) {
        Client.getCharacter().getCurrentMap().sendToField(new EmotePlayMessage(Message.emoteId, Instant.now().getEpochSecond(), Client.getCharacter().getID(), Client.getAccount().id));
    }

    @HandlerAttribute(ID = StatsUpgradeRequestMessage.MESSAGE_ID)
    public static void HandleStatsUpgradeRequestMessage(WorldClient Client, StatsUpgradeRequestMessage Message) {
        //Todo StatsUpgradeResultEnum.FIGHT
        if (Message.useAdditionnal) {
            Client.send(new BasicNoOperationMessage());
            PlayerController.sendServerMessage(Client, "Not implanted yet");
            return;
        }
        StatsEnum Stat = BOOST_ID_TO_STATS.get((int) Message.statId);
        if (Stat == null) {
            throw new Error("Wrong statsid");
        }
        if (Message.boostPoint <= 0) {
            throw new Error("client given 0 as boostpoint. Forbidden value.");
        }
        int base = Client.getCharacter().getStats().getBase(Stat);
        short num1 = (short) Message.boostPoint;
        if ((int) num1 < 1 || (int) Message.boostPoint > Client.getCharacter().getStatPoints()) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        int oldbase = base;
        List<List<Integer>> thresholds = DAO.getD2oTemplates().getBreed(Client.getCharacter().breed).GetThresholds((int) Message.statId);
        for (int thresholdIndex = DAO.getD2oTemplates().getBreed(Client.getCharacter().breed).GetThresholdIndex((int) base, thresholds); (long) num1 >= (long) thresholds.get(thresholdIndex).get(1); thresholdIndex = DAO.getD2oTemplates().getBreed(Client.getCharacter().breed).GetThresholdIndex((int) base, thresholds)) {
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
        Client.getCharacter().getStats().getEffect(Stat).Base = base;
        switch ((int) Message.statId) {
            case StatsBoostEnum.Strength:
                Client.getCharacter().setStrength(base);
                break;

            case StatsBoostEnum.Vitality:
                Client.getCharacter().setVitality(base);
                Client.getCharacter().addLife(base - oldbase); // on boost la life
                break;

            case StatsBoostEnum.Wisdom:
                Client.getCharacter().setWisdom(base);
                break;

            case StatsBoostEnum.Intelligence:
                Client.getCharacter().setIntell(base);
                break;

            case StatsBoostEnum.Chance:
                Client.getCharacter().setChance(base);
                break;

            case StatsBoostEnum.Agility:
                Client.getCharacter().setAgility(base);
                break;
        }
        Client.getCharacter().addStatPoints(- (int) Message.boostPoint - num1);
        Client.send(new StatsUpgradeResultMessage(StatsUpgradeResultEnum.SUCCESS, Message.boostPoint));
        Client.getCharacter().refreshStats();
    }

    @HandlerAttribute(ID = ChangeMapMessage.MESSAGE_ID)
    public static void HandleChangeMapMessage(WorldClient Client, ChangeMapMessage Message) {

        if (Client.getCharacter().getCell() == null || !Client.getCharacter().getCell().affectMapChange()) {
            System.out.println("undefinied cell");
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
