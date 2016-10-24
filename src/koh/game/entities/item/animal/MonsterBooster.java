package koh.game.entities.item.animal;

import java.util.Arrays;
import koh.utils.Enumerable;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class MonsterBooster {

    @Getter
    public int monsterFamily, deathNumber;
    @Getter
    public int point, statsBoost;
    @Getter
    public int[] stats, fakeBoostStats;

    public MonsterBooster(int MonsterFamily, int DeathNumber, int Point, int[] Stats, int StatsPoints, String correctStat) {
        this.monsterFamily = MonsterFamily;
        this.deathNumber = DeathNumber;
        this.point = Point;
        this.stats = Stats;
        this.statsBoost = StatsPoints;
        if (correctStat != null) {
            this.fakeBoostStats = Enumerable.stringToIntArray(correctStat, ":");
        }
    }

    public int getStatsBoost(int stat) {
        if (this.fakeBoostStats == null) {
            return statsBoost;
        } else {
            return this.fakeBoostStats[Arrays.binarySearch(stats, stat)];
        }
    }
    
      public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    

}
