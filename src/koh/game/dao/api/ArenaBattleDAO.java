package koh.game.dao.api;

import koh.game.entities.kolissium.ArenaBattle;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 6/9/16.
 */
public abstract class ArenaBattleDAO implements Service {
    public abstract ArenaBattle find(int id);

    public abstract void add(ArenaBattle arena);

    public abstract void remove(int id);
}
