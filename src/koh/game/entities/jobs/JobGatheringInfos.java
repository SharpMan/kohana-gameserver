package koh.game.entities.jobs;

import java.util.HashMap;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public class JobGatheringInfos {

    public HashMap<Integer, Couple<Integer, Integer>> gatheringByLevel = new HashMap<>(); //@Param1 =level , @Param2 = <Min,Max>
    public int bonusMin, bonusMax, xpEarned;

    public Couple<Integer, Integer> levelMinMax(int currentLevel) {
        return this.gatheringByLevel.get(this.gatheringByLevel.keySet().stream().filter(x -> currentLevel >= x).mapToInt(x -> x).max().getAsInt());
    }

    public Couple<Integer, Integer> toCouple(String content) {
        if (content.isEmpty()) {
            return null;
        }
        return new Couple<Integer, Integer>(Integer.parseInt(content.split("-")[0]), Integer.parseInt(content.split("-")[1]));
    }

}
