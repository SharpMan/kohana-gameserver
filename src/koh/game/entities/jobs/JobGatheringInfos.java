package koh.game.entities.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import koh.utils.Couple;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class JobGatheringInfos {

    @Getter
    private final HashMap<Integer, Couple<Integer, Integer>> gatheringByLevel = new HashMap<>(); //@Param1 =level , @Param2 = <Min,Max>
    @Getter
    private int bonusMin, bonusMax, xpEarned;

    public JobGatheringInfos(ResultSet result) throws SQLException {
        for(int i=0; i<=200; i+=20) {
            this.gatheringByLevel.put(i, this.toCouple(result.getString("level"+i)));
        }
        this.bonusMin = Integer.parseInt(result.getString("bonus").split("-")[0]);
        this.bonusMax = Integer.parseInt(result.getString("bonus").split("-")[1]);
        this.xpEarned = result.getInt("xp_earned");
    }

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
