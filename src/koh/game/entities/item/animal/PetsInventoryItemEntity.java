package koh.game.entities.item.animal;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "pets")
public class PetsInventoryItemEntity {

    @DatabaseField(columnName = "id", dataType = DataType.INTEGER, id = true)
    public int petsID;

    @DatabaseField(columnDefinition = "LONGBLOB not null", columnName = "infos", dataType = DataType.BYTE_ARRAY)
    public byte[] informations;

    @DatabaseField(columnName = "last_eat", dataType = DataType.STRING)
    public String lastEat;

    @DatabaseField(columnName = "hormone_used", dataType = DataType.INTEGER)
    public int pointsUsed;

    void totalClear() {
        try {
            this.petsID = 0;
            this.informations = null;
            this.lastEat = null;
            this.pointsUsed = 0;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
