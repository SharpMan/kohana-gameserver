package koh.game.entities.guilds;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import koh.game.dao.sqlite.GuildDAO;

/**
 *
 * @author Neo-Craft
 */
@DatabaseTable(tableName = "guilds")
public class GuildEntity {
    
    public void Save() {
        GuildDAO.Update(this);
    }


    @DatabaseField(columnName = "id", dataType = DataType.INTEGER, id = true)
    public int GuildID;

    @DatabaseField(columnName = "name", dataType = DataType.STRING)
    public String Name;

    @DatabaseField(columnName = "creation_date", dataType = DataType.INTEGER)
    public int CreationDate;

    @DatabaseField(columnName = "level", dataType = DataType.INTEGER)
    public int Level;

    @DatabaseField(columnName = "experience", dataType = DataType.STRING)
    public String Experience;

    @DatabaseField(columnName = "capital", dataType = DataType.INTEGER)
    public volatile int Boost;

    @DatabaseField(columnName = "prospecting", dataType = DataType.INTEGER)
    public int Prospecting;

    @DatabaseField(columnName = "wisdom", dataType = DataType.INTEGER)
    public int Wisdom;

    @DatabaseField(columnName = "pods", dataType = DataType.INTEGER)
    public int Pods;

    @DatabaseField(columnName = "max_tax_collectors", dataType = DataType.INTEGER)
    public int MaxTaxCollectors;

    //0,0,0,0,0,0,0,0,0,0,0,0
    @DatabaseField(columnName = "spells", dataType = DataType.STRING)
    public String Spells;

    @DatabaseField(columnName = "background_color", dataType = DataType.INTEGER)
    public int EmblemBackgroundColor;

    @DatabaseField(columnName = "background_shape", dataType = DataType.INTEGER)
    public int EmblemBackgroundShape;

    @DatabaseField(columnName = "foreground_color", dataType = DataType.INTEGER)
    public int EmblemForegroundColor;

    @DatabaseField(columnName = "foreground_shape", dataType = DataType.INTEGER)
    public int EmblemForegroundShape;
    
    public long Experience() {
        return Long.valueOf(this.Experience);
    }

    public void AddExperience(long b) {
        this.Experience = String.valueOf(Long.valueOf(this.Experience) + b);
        this.Save();
    }

}
