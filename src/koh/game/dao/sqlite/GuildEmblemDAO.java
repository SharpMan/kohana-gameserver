package koh.game.dao.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import koh.d2o.entities.EmblemSymbols;
import koh.game.MySQL;
import koh.game.utils.Settings;

/**
 *
 * @author Neo-Craft
 */
public class GuildEmblemDAO {

    public static HashMap<Integer, EmblemSymbols> Cache = new HashMap<>();

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from guilds_emblems", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new EmblemSymbols() {
                    {
                        this.idtype = RS.getInt("id");
                        this.categoryIdtype = RS.getInt("category_id");
                        this.iconIdtype = RS.getInt("icon_id");
                        this.skinIdtype = RS.getInt("skin_id");
                        this.ordertype = RS.getInt("order");
                        this.colorizabletype = RS.getBoolean("colorizable");

                    }
                });

                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

}
