package koh.game.dao.api;

import koh.game.entities.achievement.AchievementTemplate;
import koh.game.entities.actors.character.AchievementBook;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 12/27/16.
 */
public abstract class AchievementDAO implements Service  {
    public abstract void loadPlayerBook(AchievementBook book);

    public abstract void saveBook(AchievementBook book);

    public abstract int loadRewards();

    public abstract int loadGoals();

    public abstract int loadAll();

    public abstract AchievementTemplate find(int id);
}
