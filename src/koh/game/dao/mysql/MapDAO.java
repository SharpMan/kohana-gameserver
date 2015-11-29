package koh.game.dao.mysql;

import koh.game.entities.environments.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import koh.game.Main;
import koh.game.MySQL;
import koh.game.utils.Settings;
import koh.protocol.types.game.house.HouseInformations;
import koh.protocol.types.game.interactive.StatedElement;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Neo-Craft
 */
public class MapDAO {

    public static Map<Integer, DofusMap> Cache = new HashMap<>();

    public static Map<Integer, DofusZaap> Zaaps = new HashMap<>(30);
    public static Map<Integer, ArrayList<DofusZaap>> SubWays = new HashMap<>(); //@Param1 = AreaId , ^Param2 = List of Subways

    public static int FindAll() {
        try {
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_data order by id desc;", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                //System.out.println(RS.getInt("id"));
                Cache.put(RS.getInt("id"), new DofusMap(RS.getInt("id"), (byte) RS.getInt("version"), RS.getInt("relativeid"), (byte) RS.getInt("maptype"), RS.getInt("subareaid"), RS.getInt("bottomneighbourId"), RS.getInt("topneighbourid"), RS.getInt("leftneighbourId"), RS.getInt("rightneighbourId"), RS.getInt("shadowbonusonentities"), RS.getByte("uselowpassfilter") == 1, RS.getByte("usereverb") == 1, RS.getInt("presetid"), RS.getString("bluecells"), RS.getString("redcells"), RS.getBytes("cells"), RS.getBytes("layers")));
                if (RS.getString("new_neighbour") != null) {
                    Cache.get(RS.getInt("id")).newNeighbour = new NeighBourStruct[]
                    {
                        new NeighBourStruct(Integer.parseInt(RS.getString("new_neighbour").split(",")[0].split(":")[0]), Integer.parseInt(RS.getString("new_neighbour").split(",")[0].split(":")[1])),
                        new NeighBourStruct(Integer.parseInt(RS.getString("new_neighbour").split(",")[1].split(":")[0]), Integer.parseInt(RS.getString("new_neighbour").split(",")[1].split(":")[1])),
                        new NeighBourStruct(Integer.parseInt(RS.getString("new_neighbour").split(",")[2].split(":")[0]), Integer.parseInt(RS.getString("new_neighbour").split(",")[2].split(":")[1])),
                        new NeighBourStruct(Integer.parseInt(RS.getString("new_neighbour").split(",")[3].split(":")[0]), Integer.parseInt(RS.getString("new_neighbour").split(",")[3].split(":")[1]))
                     };
                }
            }
            MySQL.closeResultSet(RS);
            return Cache.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static DofusZaap GetSubWay(int sub, int mapid) {
        try {
            return SubWays.get(sub).stream().filter(x -> x.Mapid == mapid).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public static int FindSubways() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_subway", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    if (SubWays.get(AreaDAO.SubAreas.get(RS.getInt("subarea")).area.id) == null) {
                        SubWays.put(AreaDAO.SubAreas.get(RS.getInt("subarea")).area.id, new ArrayList<>());
                    }

                    SubWays.get(AreaDAO.SubAreas.get(RS.getInt("subarea")).area.id).add(new DofusZaap() {
                        {
                            this.Mapid = RS.getInt("mapid");
                            this.Cell = RS.getShort("cell");
                            this.SubArea = RS.getInt("subarea");
                        }
                    });
                } catch (Exception e) {
                    System.out.println(RS.getInt("subarea") + " nuleld ");
                }
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindZaaps() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_zaaps", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Zaaps.put(RS.getInt("mapid"), new DofusZaap() {
                    {
                        this.Mapid = RS.getInt("mapid");
                        this.Cell = RS.getShort("cell");
                        this.SubArea = RS.getInt("subarea");
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

    //return ToStringBuilder.reflectionToString(this);
    public static int FindTriggers() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_triggers", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    Cache.get(RS.getInt("old_map")).Init();
                    Cache.get(RS.getInt("old_map")).getCell(RS.getShort("old_cell")).myAction = new DofusTrigger() {
                        {
                            this.Type = RS.getInt("type");
                            this.NewMap = RS.getInt("map");
                            this.NewCell = RS.getShort("cell");
                            this.Criteria = RS.getString("conditions");
                        }
                    };
                } catch (Exception e) {
                    Main.Logs().writeError("Map " + RS.getInt("map") + " trigger null");
                }
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int FindDoors() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_interactive_doors", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    Cache.get(RS.getInt("map")).addDoor(new MapDoor() {
                        {
                            this.ElementID = RS.getInt("elem_id");
                            this.Map = RS.getInt("map");
                            this.Type = RS.getInt("type");
                            this.Parameters = RS.getString("parameters");
                            this.Criteria = RS.getString("criteria");
                        }
                    });
                } catch (Exception e) {
                    Main.Logs().writeError("Map " + RS.getInt("map") + " Door nulled");
                }
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindHouseInformations() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_houses", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                Cache.get(RS.getInt("map")).Houses.add(new HouseInformations() {
                    {
                        this.doorsOnMap = new int[RS.getString("doors_on_map").split(",").length];
                        for (int i = 0; i < RS.getString("doors_on_map").split(",").length; i++) {
                            this.doorsOnMap[i] = Integer.parseInt(RS.getString("doors_on_map").split(",")[i]);
                        }
                        houseId = RS.getInt("id");
                        ownerName = RS.getString("owner");
                        isOnSale = RS.getBoolean("on_sale");
                        isSaleLocked = RS.getBoolean("is_sale_locked");
                        modelId = RS.getInt("model");
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

    public static int FindSatedElements() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_stated_elements", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    //synchronized (Cache.get(RS.getInt("map")).ElementsStated) {
                    Cache.get(RS.getInt("map")).ElementsStated = ArrayUtils.add(Cache.get(RS.getInt("map")).ElementsStated, new StatedElement(RS.getInt("element_id"), RS.getShort("element_cell"), 0));
                    //}
                } catch (Exception e) {
                    Main.Logs().writeError("Map " + RS.getInt("map") + " stated nulled");
                }
                i++;
            }
            MySQL.closeResultSet(RS);
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static DofusMap GetMapByPos(int X, int Y) {
        try {
            return Cache.values().stream().filter(x -> x.Position != null && x.Position.posX == X && x.Position.posY == Y).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public static MapPosition[] GetSubAreaOfPos(int X, int Y) {
        return Cache.values().stream().filter(x -> x.Position != null && x.Position.posX == X && x.Position.posY == Y).map(x -> x.Position).toArray(MapPosition[]::new);
    }

    public static DofusMap GetMapByPos(int X, int Y, int subArea) {
        try {
            return Cache.values().stream().filter(x -> x.Position != null && x.Position.posX == X && x.Position.posY == Y && x.Position.subAreaId == subArea).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    public static int FindMapPositions() {
        try {
            int i = 0, i2 = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from map_positions", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    Cache.get(RS.getInt("id")).Position = new MapPosition() {
                        {
                            id = RS.getInt("id");
                            posX = (short) RS.getInt("posX");
                            posY = (short) RS.getInt("posY");
                            outdoor = RS.getBoolean("outdoor");
                            nameId = RS.getString("name");
                            showNameOnFingerpost = RS.getBoolean("show_name_on_finger_post");
                            subAreaId = RS.getInt("subarea_id");
                            worldMap = RS.getInt("wold_map");
                            hasPriorityOnWorldmap = RS.getBoolean("has_priority_on_world_map");
                        }
                    };
                } catch (Exception e) {
                    i2++;
                }
                i++;
            }
            MySQL.closeResultSet(RS);
            if (i2 > 0) {
                Main.Logs().writeInfo(i2 + " MapPositions uncatched");
            }
            return i;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static int FindInteractiveElements() {
        try {
            int i = 0;
            ResultSet RS = MySQL.executeQuery("SELECT * from maps_interactive_elements;", Settings.GetStringElement("Database.Name"), 0);

            while (RS.next()) {
                try {
                    Cache.get(RS.getInt("map")).InteractiveElements.add(new InteractiveElementStruct(RS.getInt("element_id"), RS.getInt("element_type_id"), RS.getString("enabled_skills"), RS.getString("disabled_skills"), RS.getShort("age_bonus")));

                } catch (Exception e) {
                    Main.Logs().writeError("Map " + RS.getInt("map") + " element nulled");
                }

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
