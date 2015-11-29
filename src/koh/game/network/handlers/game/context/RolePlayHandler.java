package koh.game.network.handlers.game.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import koh.game.Main;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.D2oDao;
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

    @HandlerAttribute(ID = EmotePlayRequestMessage.MESSAGE_ID)
    public static void EmotePlayRequestMessage(WorldClient Client, EmotePlayRequestMessage Message) {
        Client.Character.CurrentMap.sendToField(new EmotePlayMessage(Message.emoteId, Instant.now().getEpochSecond(), Client.Character.ID, Client.getAccount().ID));
    }

    @HandlerAttribute(ID = StatsUpgradeRequestMessage.MESSAGE_ID)
    public static void HandleStatsUpgradeRequestMessage(WorldClient Client, StatsUpgradeRequestMessage Message) {
        //Todo StatsUpgradeResultEnum.FIGHT
        if (Message.useAdditionnal) {
            Client.Send(new BasicNoOperationMessage());
            PlayerController.SendServerMessage(Client, "Not implanted yet");
            return;
        }
        StatsEnum Stat = BOOST_ID_TO_STATS.get((int) Message.statId);
        if (Stat == null) {
            throw new Error("Wrong statsid");
        }
        if (Message.boostPoint <= 0) {
            throw new Error("Client given 0 as boostpoint. Forbidden value.");
        }
        int base = Client.Character.Stats.GetBase(Stat);
        short num1 = (short) Message.boostPoint;
        if ((int) num1 < 1 || (int) Message.boostPoint > Client.Character.StatPoints) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        int oldbase = base;
        List<List<Integer>> thresholds = D2oDao.getBreed(Client.Character.Breed).GetThresholds((int) Message.statId);
        for (int thresholdIndex = D2oDao.getBreed(Client.Character.Breed).GetThresholdIndex((int) base, thresholds); (long) num1 >= (long) thresholds.get(thresholdIndex).get(1); thresholdIndex = D2oDao.getBreed(Client.Character.Breed).GetThresholdIndex((int) base, thresholds)) {
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
        Client.Character.Stats.GetEffect(Stat).Base = base;
        switch ((int) Message.statId) {
            case StatsBoostEnum.Strength:
                Client.Character.Strength = base;
                break;

            case StatsBoostEnum.Vitality:
                Client.Character.Vitality = base;
                Client.Character.Life += (base - oldbase); // on boost la life
                break;

            case StatsBoostEnum.Wisdom:
                Client.Character.Wisdom = base;
                break;

            case StatsBoostEnum.Intelligence:
                Client.Character.Intell = base;
                break;

            case StatsBoostEnum.Chance:
                Client.Character.Chance = base;
                break;

            case StatsBoostEnum.Agility:
                Client.Character.Agility = base;
                break;
        }
        Client.Character.StatPoints -= ((int) Message.boostPoint - num1);
        Client.Send(new StatsUpgradeResultMessage(StatsUpgradeResultEnum.SUCCESS, Message.boostPoint));
        Client.Character.RefreshStats();
    }

    @HandlerAttribute(ID = ChangeMapMessage.MESSAGE_ID)
    public static void HandleChangeMapMessage(WorldClient Client, ChangeMapMessage Message) {

        if (Client.Character.Cell == null || !Client.Character.Cell.AffectMapChange()) {
            System.out.println("undefinied cell");
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        //Client.SequenceMessage();
        //Client.sendPacket(new BasicNoOperationMessage());

        //System.out.println(cell.MapChangeData + "cell" + cell.Id);
        if (Client.Character.CurrentMap.TopNeighbourId == Message.mapId) {
            Client.Character.teleport(Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[0].Mapid : Message.mapId, Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[0].Cellid : (Client.Character.Cell.Id + 532));
        } else if (Client.Character.CurrentMap.BottomNeighbourId == Message.mapId) {
            Client.Character.teleport(Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[1].Mapid : Message.mapId, Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[1].Cellid : (Client.Character.Cell.Id - 532));
        } else if (Client.Character.CurrentMap.LeftNeighbourId == Message.mapId) {
            Client.Character.teleport(Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[2].Mapid : Message.mapId, Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[2].Cellid : (Client.Character.Cell.Id + 13));
        } else if (Client.Character.CurrentMap.RightNeighbourId == Message.mapId) {
            Client.Character.teleport(Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[3].Mapid : Message.mapId, Client.Character.CurrentMap.newNeighbour != null ? Client.Character.CurrentMap.newNeighbour[3].Cellid : (Client.Character.Cell.Id - 13));
        } else {
            // Client.Character.teleport(Message.mapId, -1);
            Main.Logs().writeError("Client " + Client.Character.NickName + " teleport from " + Client.Character.CurrentMap.Id + " to " + Message.mapId);
            Client.Send(new BasicNoOperationMessage());
            //System.out.println("undefinied map");
        }

    }

}
