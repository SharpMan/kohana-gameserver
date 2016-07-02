package koh.game.dao.api;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.preset.PresetBook;
import koh.game.entities.actors.character.preset.PresetEntity;
import koh.patterns.services.api.Service;
import koh.protocol.types.game.inventory.preset.Preset;

/**
 * Created by Melancholia on 7/1/16.
 */
public abstract class PresetDAO implements Service {

    public abstract void insert( PresetEntity entity);

    public abstract void update(int owner, PresetEntity entity);

    public abstract void remove(int owner , byte id);

    public abstract PresetBook get(Player owner);

}
