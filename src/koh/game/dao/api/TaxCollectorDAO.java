package koh.game.dao.api;

import koh.game.entities.actors.TaxCollector;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 12/4/16.
 */
public abstract class TaxCollectorDAO implements Service {

    public abstract int loadAll();

    public abstract void update(TaxCollector tax);

    public abstract void updateSummmary(TaxCollector tax);

    public abstract void remove(int iden);

    public abstract void removeGuild(int guild);

    public abstract boolean insert(TaxCollector tax);
    public abstract TaxCollector find(int map);

    public abstract boolean isPresentOn(int map);
}
