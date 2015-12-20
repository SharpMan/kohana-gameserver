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

    private static final Logger logger = LogManager.getLogger(MapDAO.class);

    private final Map<Integer, DofusMap> dofusMaps = new HashMap<>(5000);
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
                    String[] neighbours = result.getString("new_neighbour").split(",");
                    dofusMaps.get(result.getInt("id")).setNewNeighbour(new NeighBourStruct[]
                            {
                                    new NeighBourStruct(neighbours[0].split(":")),
                                    new NeighBourStruct(neighbours[1].split(":")),
                                    new NeighBourStruct(neighbours[2].split(":")),
                                    new NeighBourStruct(neighbours[3].split(":"))
                            });
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
        return zaaps.entrySet().stream().filter(zaap -> zaap.getValue().getMapid() != id);
    }

    @Override
    public ArrayList<DofusZaap> getSubway(int id){
        return subWays.get(id);
    }

    @Override
    public DofusZaap findSubWay(int sub, int mapid) {
        return subWays.get(sub).stream().filter(x -> x.getMapid() == mapid).findFirst().orElse(null);
    }

    private int loadAllSubways() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_subway", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    if (subWays.get(DAO.getAreas().getSubArea(result.getInt("subarea")).getArea().getId()) == null) {
                        subWays.put(DAO.getAreas().getSubArea(result.getInt("subarea")).getArea().getId(), new ArrayList<>());
                    }

                    subWays.get(DAO.getAreas().getSubArea(result.getInt("subarea")).getArea().getId()).add(new DofusZaap(result));
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
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_zaaps", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                zaaps.put(result.getInt("mapid"), new DofusZaap(result));
            }
            MySQL.closeResultSet(result);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return zaaps.size();
    }

    private int loadAllTriggers() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_triggers", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("old_map")).initialize();
                    dofusMaps.get(result.getInt("old_map")).getCell(result.getShort("old_cell")).myAction = new DofusTrigger(result);
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
                    dofusMaps.get(result.getInt("map")).addDoor(new MapDoor(result));
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
                dofusMaps.get(result.getInt("map")).getHouses().add(new HouseInformations() {
                    {
                        String[] doorsInfos = result.getString("doors_on_map").split(",");
                        this.doorsOnMap = new int[doorsInfos.length];
                        for (int i = 0; i < doorsInfos.length; i++)
                            this.doorsOnMap[i] = Integer.parseInt(doorsInfos[i]);

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
                    dofusMaps.get(result.getInt("map")).setElementsStated(ArrayUtils.add(dofusMaps.get(result.getInt("map")).getElementsStated(), new StatedElement(result.getInt("element_id"), result.getShort("element_cell"), 0)));
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

    @Override
    public DofusMap findMapByPos(int X, int Y) {
        return dofusMaps.values().stream().filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y)
                .findFirst().orElse(null);
    }

    @Override
    public MapPosition[] getSubAreaOfPos(int X, int Y) {
        return dofusMaps.values().stream().filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y).map(x -> x.getPosition()).toArray(MapPosition[]::new);
    }

    @Override
    public DofusMap findMapByPos(int X, int Y, int subArea) {
        return dofusMaps.values().stream()
                .filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y && x.getPosition().getSubAreaId() == subArea).
                        findFirst().orElse(null);

    }

    private int loadAllPositions() {
        int i = 0, i2 = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_positions", 0)) {
            ResultSet result = conn.getResult();


            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("id")).setPosition(new MapPosition(result));
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
                    dofusMaps.get(result.getInt("map")).getInteractiveElements().add(new InteractiveElementStruct(result.getInt("element_id"), result.getInt("element_type_id"), result.getString("enabled_skills"), result.getString("disabled_skills"), result.getShort("age_bonus")));
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
