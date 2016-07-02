package koh.game.entities.actors.character.preset;

import koh.game.dao.DAO;
import koh.protocol.types.game.inventory.preset.Preset;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 7/1/16.
 */
public class PresetBook {

    private final Map<Byte, Preset> book = Collections.synchronizedMap(new HashMap<>(4));
    private final Map<Byte, PresetEntity> entities = Collections.synchronizedMap(new HashMap<>(4));

    public PresetBook(){

    }

    public void add(Preset preset){
        this.book.put(preset.presetId,preset);
    }

    public void add(Preset preset,PresetEntity entity){
        this.book.put(preset.presetId,preset);
        this.entities.put(preset.presetId,entity);
    }

    public Stream<Preset> getValues(){
        return book.values().stream();
    }

    public int size(){
        return book.size();
    }

    public Preset get(byte id){
        return this.book.get(id);
    }

    public PresetEntity getEntity(byte id){
        return this.entities.get(id);
    }

    public boolean remove (byte id, int owner){
        DAO.getPresets().remove(owner,id);
        entities.remove(id);
        return book.remove(id) != null;
    }


}
