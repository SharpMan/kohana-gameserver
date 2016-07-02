package koh.game.entities.actors.character.preset;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import koh.protocol.types.game.inventory.preset.Preset;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Melancholia on 7/1/16.
 */
@DatabaseTable(tableName = "preset")
public class PresetEntity {

    //public void save() { DAO.getGuildMembers().update(this); }
    @DatabaseField(columnName = "owner", dataType = DataType.INTEGER)
    public int owner;

    @DatabaseField(columnName = "id", dataType = DataType.INTEGER)
    public int id;

    @DatabaseField(columnDefinition = "LONGBLOB", columnName = "infos", dataType = DataType.BYTE_ARRAY)
    public byte[] informations;

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
