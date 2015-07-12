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

    public int NextFightId() {
        if (this.myFights.isEmpty()) {
            return 1;
        }
        return this.myFights.stream().mapToInt(x -> x.FightId).max().getAsInt() + 1;
    }

    public int FightCount() {
        return this.myFights.size();
    }

    public List<Fight> Fights() {
        return this.myFights;
    }

    public Fight GetFight(int FightId) {
        synchronized (this.myFights) {
            for (Fight Fight : this.myFights) {
                if (Fight.FightId == FightId) {
                    return Fight;
                }
            }
            return null;
        }
    }

    public void SendFightInfos(WorldClient Client) {
        this.myFights.stream().filter((Fight) -> (Fight.FightState == FightState.STATE_PLACE)).forEach((Fight) -> {
            Fight.SendFightFlagInfos(Client);
        });
        Client.Send(new MapFightCountMessage(this.myFights.size()));
    }

    public void AddFight(Fight Fight) {
        this.myFights.add(Fight);
    }

    public void RemoveFight(Fight Fight) {
        this.myFights.remove(Fight);
    }

}
