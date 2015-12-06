package koh.game.dao.api;

import koh.game.entities.item.animal.PetsInventoryItemEntity;
import koh.patterns.services.api.Service;

public abstract class PetInventoryDAO implements Service {

    public abstract int nextId();

    public abstract void insert(PetsInventoryItemEntity entity);

    public abstract void update(PetsInventoryItemEntity entity);

    public abstract PetsInventoryItemEntity get(int id);

}
