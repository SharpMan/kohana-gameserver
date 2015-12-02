package koh.game.fights;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.roleplay.MapFightCountMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightController {

    private CopyOnWriteArrayList<Fight> myFights = new CopyOnWriteArrayList<>();

    public int nextFightId() {
        if (this.myFights.isEmpty()) {
            return 1;
        }
        return this.myFights.stream().mapToInt(x -> x.fightId).max().getAsInt() + 1;
    }

    public int fightCount() {
        return this.myFights.size();
    }

    public List<Fight> getFights() {
        return this.myFights;
    }

    public Fight getFight(int FightId) {
        synchronized (this.myFights) {
            for (Fight Fight : this.myFights) {
                if (Fight.fightId == FightId) {
                    return Fight;
                }
            }
            return null;
        }
    }

    public void sendFightInfos(WorldClient Client) {
        this.myFights.stream().filter((Fight) -> (Fight.fightState == FightState.STATE_PLACE)).forEach((Fight) -> {
            Fight.sendFightFlagInfos(Client);
        });
        Client.send(new MapFightCountMessage(this.myFights.size()));
    }

    public void addFight(Fight Fight) {
        this.myFights.add(Fight);
    }

    public void removeFight(Fight Fight) {
        this.myFights.remove(Fight);
    }

}
