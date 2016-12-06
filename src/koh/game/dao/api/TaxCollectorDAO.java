package koh.game.dao.api;

import koh.game.entities.actors.TaxCollector;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 12/4/16.
 */
public abstract class TaxCollectorDAO implements Service {

    public abstract int loadAll();
    public abstract void remove(int iden);
    public abstract void insert(TaxCollector tax);
    public abstract TaxCollector find(int map);

}
