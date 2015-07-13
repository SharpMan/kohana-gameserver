package koh.game.entities.item.animal;

import java.util.Arrays;
import koh.utils.Enumerable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class MonsterBooster {

    public int MonsterFamily, DeathNumber;
    public int Point;
    private int StatsBoost;
    public int[] Stats;
    public int[] FakeBoostStats;

    public MonsterBooster(int MonsterFamily, int DeathNumber, int Point, int[] Stats, int StatsPoints, String CorrectStat) {
        this.MonsterFamily = MonsterFamily;
        this.DeathNumber = DeathNumber;
        this.Point = Point;
        this.Stats = Stats;
        this.StatsBoost = StatsPoints;
        if (CorrectStat != null) {
            this.FakeBoostStats = Enumerable.StringToIntArray(CorrectStat, ":");
        }
    }

    public int getStatsBoost(int Stat) {
        if (this.FakeBoostStats == null) {
            return StatsBoost;
        } else {
            return this.FakeBoostStats[Arrays.binarySearch(Stats, Stat)];
        }
    }
    
      public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    

}
