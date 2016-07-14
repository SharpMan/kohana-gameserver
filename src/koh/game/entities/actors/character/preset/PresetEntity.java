package koh.game.entities.actors.character.preset;

import koh.protocol.types.game.inventory.preset.Preset;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Melancholia on 7/1/16.
 * TODO: fusionate with PresetType
 */
@AllArgsConstructor
public class PresetEntity {
    @Getter @Setter
    private int owner,id;
    @Getter @Setter
    private byte[] informations;

    @Getter @Setter
    private Preset preset;


    public void totalClear() {
        try {
            this.owner = 0;
            this.informations = null;
            this.preset = null; //TODO clear preset
            this.finalize();
        } catch (Throwable tr) {
        }
    }


}
