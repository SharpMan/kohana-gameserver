package koh.game.dao.api;

import koh.game.entities.mob.MonsterTemplate;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class MonsterDAO implements Service {
    public abstract MonsterTemplate find(int id);
}
