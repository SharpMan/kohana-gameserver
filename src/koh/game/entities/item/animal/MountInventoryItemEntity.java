package koh.game.entities.item.animal;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "mounts")
public class MountInventoryItemEntity {
    
    @DatabaseField(columnName = "id", dataType = DataType.INTEGER, id = true)
    public int AnimalID;

    @DatabaseField(columnDefinition = "LONGBLOB", columnName = "infos", dataType = DataType.BYTE_ARRAY)
    public byte[] informations;

    @DatabaseField(columnName = "last_eat", dataType = DataType.STRING)
    public String lastEat;



    public void totalClear() {
        try {
            this.AnimalID = 0;
            this.informations = null;
            this.lastEat = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }
    
}
