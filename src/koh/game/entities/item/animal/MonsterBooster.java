package koh.game.entities.item.animal;

/**
 *
 * @author Neo-Craft
 */
public class MonsterBooster {

    public int MonsterFamily, DeathNumber;
    public int Point, StatsBoost;
    public int[] Stats;

    public MonsterBooster(int MonsterFamily, int DeathNumber, int Point, int[] Stats, int StatsPoints) {
        this.MonsterFamily = MonsterFamily;
        this.DeathNumber = DeathNumber;
        this.Point = Point;
        this.Stats = Stats;
        this.StatsBoost = StatsPoints;
    }

}
