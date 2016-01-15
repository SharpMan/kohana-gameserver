package koh.game.dao.api;

import koh.game.entities.mob.IAMind;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 1/13/16.
 */
public abstract class MonsterMindDAO implements Service {

    public abstract IAMind find(int id);
}
