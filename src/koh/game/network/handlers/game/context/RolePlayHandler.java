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

    public static Map<Integer, StatsEnum> BOOST_ID_TO_STATS = new HashMap<Integer, StatsEnum>() {
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
        Client.character.currentMap.sendToField(new EmotePlayMessage(Message.emoteId, Instant.now().getEpochSecond(), Client.character.ID, Client.getAccount().id));
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
        int base = Client.character.stats.getBase(Stat);
        short num1 = (short) Message.boostPoint;
        if ((int) num1 < 1 || (int) Message.boostPoint > Client.character.statPoints) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        int oldbase = base;
        List<List<Integer>> thresholds = DAO.getD2oTemplates().getBreed(Client.character.breed).GetThresholds((int) Message.statId);
        for (int thresholdIndex = DAO.getD2oTemplates().getBreed(Client.character.breed).GetThresholdIndex((int) base, thresholds); (long) num1 >= (long) thresholds.get(thresholdIndex).get(1); thresholdIndex = DAO.getD2oTemplates().getBreed(Client.character.breed).GetThresholdIndex((int) base, thresholds)) {
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
        Client.character.stats.getEffect(Stat).Base = base;
        switch ((int) Message.statId) {
            case StatsBoostEnum.Strength:
                Client.character.strength = base;
                break;

            case StatsBoostEnum.Vitality:
                Client.character.vitality = base;
                Client.character.life += (base - oldbase); // on boost la life
                break;

            case StatsBoostEnum.Wisdom:
                Client.character.wisdom = base;
                break;

            case StatsBoostEnum.Intelligence:
                Client.character.intell = base;
                break;

            case StatsBoostEnum.Chance:
                Client.character.chance = base;
                break;

            case StatsBoostEnum.Agility:
                Client.character.agility = base;
                break;
        }
        Client.character.statPoints -= ((int) Message.boostPoint - num1);
        Client.send(new StatsUpgradeResultMessage(StatsUpgradeResultEnum.SUCCESS, Message.boostPoint));
        Client.character.refreshStats();
    }

    @HandlerAttribute(ID = ChangeMapMessage.MESSAGE_ID)
    public static void HandleChangeMapMessage(WorldClient Client, ChangeMapMessage Message) {

        if (Client.character.cell == null || !Client.character.cell.affectMapChange()) {
            System.out.println("undefinied cell");
            Client.send(new BasicNoOperationMessage());
            return;
        }
        //client.sequenceMessage();
        //client.sendPacket(new BasicNoOperationMessage());

        //System.out.println(cell.mapChangeData + "cell" + cell.id);
        if (Client.character.currentMap.topNeighbourId == Message.mapId) {
            Client.character.teleport(Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[0].mapid : Message.mapId, Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[0].cellid : (Client.character.cell.id + 532));
        } else if (Client.character.currentMap.bottomNeighbourId == Message.mapId) {
            Client.character.teleport(Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[1].mapid : Message.mapId, Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[1].cellid : (Client.character.cell.id - 532));
        } else if (Client.character.currentMap.leftNeighbourId == Message.mapId) {
            Client.character.teleport(Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[2].mapid : Message.mapId, Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[2].cellid : (Client.character.cell.id + 13));
        } else if (Client.character.currentMap.rightNeighbourId == Message.mapId) {
            Client.character.teleport(Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[3].mapid : Message.mapId, Client.character.currentMap.newNeighbour != null ? Client.character.currentMap.newNeighbour[3].cellid : (Client.character.cell.id - 13));
        } else {
            // client.character.teleport(Message.mapId, -1);
            logger.error("client {} teleport from {} to {}" ,Client.character.nickName,Client.character.currentMap.id, Message.mapId);
            Client.send(new BasicNoOperationMessage());
            //System.out.println("undefinied map");
        }

    }

}
