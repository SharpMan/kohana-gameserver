package koh.game.entities.achievement;

import lombok.Builder;
import lombok.Getter;

/**
 * Created by Melancholia on 12/27/16.
 */
@Builder
public class AchievementReward {

    @Getter
    private int id, achievementId;
    @Getter
    private int levelMin,levelMax;
    @Getter
    private int[] itemsReward, itemsQuantityReward, emotesReward, spellsReward, titlesReward, ornamentsReward;

    public boolean pass(int level){
        return (levelMin == -1 || level >= levelMin) && (levelMax == -1 && level <= level);
    }

}
