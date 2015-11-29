package koh.game.dao.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import koh.game.entities.item.animal.MountTemplate;
import java.util.HashMap;
import koh.game.MySQL;
import koh.game.utils.Settings;
import koh.look.EntityLookParser;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.utils.Couple;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class MountDAO {

    public static HashMap<Integer, MountTemplate> Cache = new HashMap<>();

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from mount_templates", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("scroll_id"), new MountTemplate() {
                    {
                        this.Id = RS.getInt("id");
                        this.ScroolId =RS.getInt("scroll_id");
                        this.Look = EntityLookParser.fromString(RS.getString("look"));
                        if (!RS.getString("stats").isEmpty()) {
                            for (String stat : RS.getString("stats").split("\\|")) {
                                String[] infos = stat.split("=");
                                Couple<Integer, Double> c = new Couple<Integer, Double>(Integer.parseInt(infos[0]), (infos.length > 1 ? Double.parseDouble(infos[1]) : 0));
                                Stats.add(c);
                            }
                        }
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

    public static MountTemplate Model(int Model) {
        return Cache.values().stream().filter(x -> x.Id == Model).findFirst().get();
    }

    public static ObjectEffectInteger[] MountByEffect(int Model, int Level) {
        ObjectEffectInteger[] Effects = new ObjectEffectInteger[0];
        for (Couple<Integer, Double> Stat : Cache.values().stream().filter(x -> x.Id == Model).findFirst().get().Stats) {
            if ((int) (Level / Stat.second) <= 0) {
                continue;
            }
            Effects = ArrayUtils.add(Effects, new ObjectEffectInteger(Stat.first, (int) (Level / Stat.second)));
        }
        return Effects;
    }

}
