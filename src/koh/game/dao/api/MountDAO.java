package koh.game.dao.api;

import koh.game.entities.item.animal.MountTemplate;
import koh.patterns.services.api.Service;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;

/**
 * Created by Melancholia on 12/2/15.
 */
public abstract class MountDAO implements Service {

    public abstract MountTemplate findTemplate(int model);

    public abstract MountTemplate find(int id);

    public abstract ObjectEffectInteger[] getMountByEffect(int model, int level);

}
