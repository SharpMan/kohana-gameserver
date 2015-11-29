package koh.game.dao.api;

import koh.game.entities.ExpLevel;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class ExpDAO implements Service {

    public abstract ExpLevel getLevel(int level);

    public abstract long getPlayerMinExp(int level);

    public abstract long getPlayerMaxExp(int level);

    public abstract int getMaxLevel();

}
