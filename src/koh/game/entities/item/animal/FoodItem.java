package koh.game.entities.item.animal;

import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class FoodItem {

    @Getter
    private int itemID, point, stats, statsPoints;

    public FoodItem(int ItemID, int Point, int Stats, int StatsPoints) {
        this.itemID = ItemID;
        this.point = Point;
        this.stats = Stats;
        this.statsPoints = StatsPoints;
    }
    
    

}
