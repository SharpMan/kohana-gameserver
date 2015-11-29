package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import koh.game.MySQL;
import koh.game.entities.environments.Area;
import koh.game.entities.environments.SubArea;
import koh.game.entities.environments.SuperArea;
import koh.game.utils.Settings;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class AreaDAO {

    public static HashMap<Integer, SuperArea> SuperAreas = new HashMap<>(5);
    public static HashMap<Integer, Area> Cache = new HashMap<>();
    public static HashMap<Integer, SubArea> SubAreas = new HashMap<>();

    public static int FindAll() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from areas", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.put(RS.getInt("id"), new Area() {
                    {
                        this.id = RS.getInt("id");
                        this.superArea = SuperAreas.get(RS.getInt("super_area"));
                        this.containHouses = RS.getBoolean("contain_houses");
                        this.containPaddocks = RS.getBoolean("contain_paddocks");
                        this.worldmapId = RS.getInt("world_map_id");
                        this.hasWorldMap = RS.getBoolean("has_world_map");
                        SuperAreas.get(RS.getInt("super_area")).Areas = ArrayUtils.add(SuperAreas.get(RS.getInt("super_area")).Areas, this);
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

    public static int FindSubAreas() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from sub_areas", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                SubAreas.put(RS.getInt("id"), new SubArea() {
                    {
                        this.Id = RS.getInt("id");
                        this.area = Cache.get(RS.getInt("area_id"));
                        this.mapIds = Enumerable.StringToIntArray(RS.getString("map_ids"));
                        this.shape = Enumerable.StringToIntArray(RS.getString("shape"));
                        this.customWorldMaptype = Enumerable.StringToIntArray(RS.getString("custom_world_map"));
                        this.packId = RS.getInt("pack_id");
                        this.level = RS.getInt("level");
                        this.isConquestVillage = RS.getBoolean("is_conquest_village");
                        this.basicAccountAllowed = RS.getBoolean("basic_account_allowed");
                        this.displayOnWorldMap = RS.getBoolean("display_on_world_map");
                        this.monsters = Enumerable.StringToIntArray(RS.getString("monsters"));
                        this.entranceMapIds = Enumerable.StringToIntArray(RS.getString("entrance_map_ids"));
                        this.exitMapIds = Enumerable.StringToIntArray(RS.getString("exit_map_ids"));
                        this.capturable = RS.getBoolean("capturable");
                        Cache.get(RS.getInt("area_id")).SubAreas = ArrayUtils.add(Cache.get(RS.getInt("area_id")).SubAreas, this);
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

    public static int FindSuper() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from super_areas", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                SuperAreas.put(RS.getInt("id"), new SuperArea() {
                    {
                        this.Id = RS.getInt("id");
                        this.worldmapIdtype = RS.getInt("world_map_id");
                        this.hasWorldMaptype = RS.getBoolean("has_world_map");
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
