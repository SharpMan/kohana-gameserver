package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.Main;
import koh.game.MySQL;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MapDAO;
import koh.game.entities.environments.*;
import koh.game.utils.sql.ConnectionResult;
import koh.protocol.types.game.house.HouseInformations;
import koh.protocol.types.game.interactive.StatedElement;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Neo-Craft
 */
public class MapDAOImpl extends MapDAO {

    private static final Logger logger = LogManager.getLogger(MapDAOImpl.class);
    private final Map<Integer, DofusMap> dofusMaps = new HashMap<>();
    private final Map<Integer, DofusZaap> zaaps = new HashMap<>(30);
    private final Map<Integer, ArrayList<DofusZaap>> subWays = new HashMap<>(40); //@Param1 = AreaId , ^Param2 = List of Subways
    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_data order by id desc;", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                dofusMaps.put(result.getInt("id"), new DofusMap(result.getInt("id"), (byte) result.getInt("version"), result.getInt("relativeid"), (byte) result.getInt("maptype"), result.getInt("subareaid"), result.getInt("bottomneighbourId"), result.getInt("topneighbourid"), result.getInt("leftneighbourId"), result.getInt("rightneighbourId"), result.getInt("shadowbonusonentities"), result.getByte("uselowpassfilter") == 1, result.getByte("usereverb") == 1, result.getInt("presetid"), result.getString("bluecells"), result.getString("redcells"), result.getBytes("cells"), result.getBytes("layers")));
                if (result.getString("new_neighbour") != null) {
                    dofusMaps.get(result.getInt("id")).newNeighbour = new NeighBourStruct[]
                            {   //TOKISS : @alleos split
                                    new NeighBourStruct(Integer.parseInt(result.getString("new_neighbour").split(",")[0].split(":")[0]), Integer.parseInt(result.getString("new_neighbour").split(",")[0].split(":")[1])),
                                    new NeighBourStruct(Integer.parseInt(result.getString("new_neighbour").split(",")[1].split(":")[0]), Integer.parseInt(result.getString("new_neighbour").split(",")[1].split(":")[1])),
                                    new NeighBourStruct(Integer.parseInt(result.getString("new_neighbour").split(",")[2].split(":")[0]), Integer.parseInt(result.getString("new_neighbour").split(",")[2].split(":")[1])),
                                    new NeighBourStruct(Integer.parseInt(result.getString("new_neighbour").split(",")[3].split(":")[0]), Integer.parseInt(result.getString("new_neighbour").split(",")[3].split(":")[1]))
                            };
                }
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return dofusMaps.size();
    }

    @Override
    public DofusZaap getZaap(int id){
        return this.zaaps.get(id);
    }

    @Override
    public int getZaapsLength()
    {
        return this.zaaps.size();
    }

    @Override
    public Stream<Map.Entry<Integer,DofusZaap>> getZaapsNot(int id){
        return zaaps.entrySet().stream().filter(zaap -> zaap.getValue().Mapid != id);
    }

    @Override
    public ArrayList<DofusZaap> getSubway(int id){
        return subWays.get(id);
    }

    @Override
    public DofusZaap findSubWay(int sub, int mapid) {
        return subWays.get(sub).stream().filter(x -> x.Mapid == mapid).findFirst().orElse(null);
    }

    private int loadAllSubways() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_subway", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    if (subWays.get(DAO.getAreas().getSubArea(result.getInt("subarea")).area.id) == null) {
                        subWays.put(DAO.getAreas().getSubArea(result.getInt("subarea")).area.id, new ArrayList<>());
                    }

                    subWays.get(DAO.getAreas().getSubArea(result.getInt("subarea")).area.id).add(new DofusZaap() {
                        {
                            this.Mapid = result.getInt("mapid");
                            this.Cell = result.getShort("cell");
                            this.SubArea = result.getInt("subarea");
                        }
                    });
                } catch (Exception e) {
                    logger.warn("{} nulled ", result.getInt("subarea"));
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllZaaps() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_zaaps", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                zaaps.put(result.getInt("mapid"), new DofusZaap() {
                    {
                        this.Mapid = result.getInt("mapid");
                        this.Cell = result.getShort("cell");
                        this.SubArea = result.getInt("subarea");
                    }
                });
                i++;
            }
            MySQL.closeResultSet(result);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    private int loadAllTriggers() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_triggers", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("old_map")).Init();
                    dofusMaps.get(result.getInt("old_map")).getCell(result.getShort("old_cell")).myAction = new DofusTrigger() {
                        {
                            this.type = result.getInt("type");
                            this.newMap = result.getInt("map");
                            this.newCell = result.getShort("cell");
                            this.criteria = result.getString("conditions");
                        }
                    };
                } catch (Exception e) {
                    logger.warn("map {} trigger null", result.getInt("map"));
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllDoors() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_interactive_doors", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).addDoor(new MapDoor() {
                        {
                            this.elementID = result.getInt("elem_id");
                            this.map = result.getInt("map");
                            this.type = result.getInt("type");
                            this.parameters = result.getString("parameters");
                            this.criteria = result.getString("criteria");
                        }
                    });
                } catch (Exception e) {
                    Main.Logs().writeError("map " + result.getInt("map") + " Door nulled");
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    private int loadAllHouseInformations() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_houses", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                dofusMaps.get(result.getInt("map")).houses.add(new HouseInformations() {
                    {
                        this.doorsOnMap = new int[result.getString("doors_on_map").split(",").length];
                        for (int i = 0; i < result.getString("doors_on_map").split(",").length; i++) {
                            this.doorsOnMap[i] = Integer.parseInt(result.getString("doors_on_map").split(",")[i]);
                        }
                        houseId = result.getInt("id");
                        ownerName = result.getString("owner");
                        isOnSale = result.getBoolean("on_sale");
                        isSaleLocked = result.getBoolean("is_sale_locked");
                        modelId = result.getInt("model");
                    }
                });
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllSatedElements() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_stated_elements", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).elementsStated = ArrayUtils.add(dofusMaps.get(result.getInt("map")).elementsStated, new StatedElement(result.getInt("element_id"), result.getShort("element_cell"), 0));
                } catch (Exception e) {
                    Main.Logs().writeError("map " + result.getInt("map") + " stated nulled");
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    public DofusMap findMapByPos(int X, int Y) {
        return dofusMaps.values().stream().filter(x -> x.position != null && x.position.posX == X && x.position.posY == Y)
                .findFirst().orElse(null);
    }

    public MapPosition[] getSubAreaOfPos(int X, int Y) {
        return dofusMaps.values().stream().filter(x -> x.position != null && x.position.posX == X && x.position.posY == Y).map(x -> x.position).toArray(MapPosition[]::new);
    }

    public DofusMap findMapByPos(int X, int Y, int subArea) {
        return dofusMaps.values().stream()
                .filter(x -> x.position != null && x.position.posX == X && x.position.posY == Y && x.position.subAreaId == subArea).
                        findFirst().orElse(null);

    }

    private int loadAllPositions() {
        int i = 0, i2 = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_positions", 0)) {
            ResultSet result = conn.getResult();


            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("id")).position = new MapPosition() {
                        {
                            id = result.getInt("id");
                            posX = (short) result.getInt("posX");
                            posY = (short) result.getInt("posY");
                            outdoor = result.getBoolean("outdoor");
                            nameId = result.getString("name");
                            showNameOnFingerpost = result.getBoolean("show_name_on_finger_post");
                            subAreaId = result.getInt("subarea_id");
                            worldMap = result.getInt("wold_map");
                            hasPriorityOnWorldmap = result.getBoolean("has_priority_on_world_map");
                        }
                    };
                } catch (Exception e) {
                    i2++;
                }
                i++;
            }
            if (i2 > 0) {
                logger.warn("{} map positions uncatched", i2);
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    private int loadAllInteractiveElements() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_interactive_elements;", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).interactiveElements.add(new InteractiveElementStruct(result.getInt("element_id"), result.getInt("element_type_id"), result.getString("enabled_skills"), result.getString("disabled_skills"), result.getShort("age_bonus")));
                } catch (Exception e) {
                    logger.error("map {} element nulled", result.getInt("map"));
                }

                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;

    }

    @Override
    public DofusMap findTemplate(int id) {
        return this.dofusMaps.get(id);
    }

    @Override
    public void start() {
        logger.info("Loaded {} dofus maps", this.loadAll());
        logger.info("Loaded {} stated elements", this.loadAllSatedElements());
        logger.info("Loaded {} interactive elements", this.loadAllInteractiveElements());
        logger.info("Loaded {} house informations", this.loadAllHouseInformations());
        logger.info("Loaded {} interactive doors", this.loadAllDoors());
        logger.info("Loaded {} map positions", this.loadAllPositions());
        logger.info("Loaded {} map zaaps", this.loadAllZaaps());
        logger.info("Loaded {} map subways", this.loadAllSubways());
        logger.info("Loaded {} map triggers", this.loadAllTriggers());
    }

    @Override
    public void stop() {

    }
}
