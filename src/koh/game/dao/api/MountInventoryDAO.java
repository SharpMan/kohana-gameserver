package koh.game.dao.api;

import koh.game.entities.item.animal.MountInventoryItemEntity;
import koh.patterns.services.api.Service;

public abstract class MountInventoryDAO implements Service {

    public abstract int nextId();

    public abstract void insert(MountInventoryItemEntity entity);

    public abstract void update(MountInventoryItemEntity entity);

    public abstract MountInventoryItemEntity get(int id);

}
