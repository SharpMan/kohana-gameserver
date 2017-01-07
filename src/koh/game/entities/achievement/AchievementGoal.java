package koh.game.entities.achievement;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Melancholia on 12/27/16.
 */
@Builder
public class AchievementGoal {

    @Getter
    private int id , achievementId, order;
    @Getter
    private String name, criterion;


}
