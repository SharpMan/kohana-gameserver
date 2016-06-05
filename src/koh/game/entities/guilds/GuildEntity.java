package koh.game.entities.guilds;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import koh.game.dao.DAO;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "guilds")
public class GuildEntity {

    public void save() {
        DAO.getGuilds().update(this);
    }

    @DatabaseField(columnName = "id", dataType = DataType.INTEGER, id = true)
    public int guildID;

    @DatabaseField(columnName = "name", dataType = DataType.STRING)
    public String name;

    @DatabaseField(columnName = "creation_date", dataType = DataType.INTEGER)
    public int creationDate;

    @DatabaseField(columnName = "level", dataType = DataType.INTEGER)
    public int level;

    @DatabaseField(columnName = "experience", dataType = DataType.STRING)
    public String experience;

    @DatabaseField(columnName = "capital", dataType = DataType.INTEGER)
    public volatile int boost;

    @DatabaseField(columnName = "prospecting", dataType = DataType.INTEGER)
    public int prospecting;

    @DatabaseField(columnName = "wisdom", dataType = DataType.INTEGER)
    public int wisdom;

    @DatabaseField(columnName = "pods", dataType = DataType.INTEGER)
    public int pods;

    @DatabaseField(columnName = "max_tax_collectors", dataType = DataType.INTEGER)
    public int maxTaxCollectors;

    //0,0,0,0,0,0,0,0,0,0,0,0
    @DatabaseField(columnName = "spells", dataType = DataType.STRING)
    public String spells;

    @DatabaseField(columnName = "background_color", dataType = DataType.INTEGER)
    public int emblemBackgroundColor;

    @DatabaseField(columnName = "background_shape", dataType = DataType.INTEGER)
    public int emblemBackgroundShape;

    @DatabaseField(columnName = "foreground_color", dataType = DataType.INTEGER)
    public int emblemForegroundColor;

    @DatabaseField(columnName = "foreground_shape", dataType = DataType.INTEGER)
    public int emblemForegroundShape;
    
    public long getExperience() {
        return Long.valueOf(this.experience);
    }

    public void addExperience(long value) {
        this.experience = String.valueOf(Long.valueOf(this.experience) + value);
        this.save();
    }

}
