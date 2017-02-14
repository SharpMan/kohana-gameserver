package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.Logs;
import koh.game.Main;
import koh.game.MySQL;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MapDAO;
import koh.game.entities.environments.*;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.house.HouseInformations;
import koh.protocol.types.game.interactive.StatedElement;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StringBuilders;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
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
                dofusMaps.put(result.getInt("id"), new DofusMap(result.getInt("id"), (byte) result.getInt("version"), result.getInt("relativeid"), (byte) result.getInt("maptype"), result.getInt("subareaid"), result.getInt("bottomneighbourId"), result.getInt("topneighbourid"), result.getInt("leftneighbourId"), result.getInt("rightneighbourId"), result.getInt("shadowbonusonentities"), result.getByte("uselowpassfilter") == 1, result.getByte("usereverb") == 1, result.getInt("presetid"), result.getString("bluecells"), result.getString("redcells"), result.getBytes("cells"), result.getBytes("layers"),result.getString("new_neighbour")));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return dofusMaps.size();
    }

    @Override
    public void updateNeighboor(DofusMap map) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `maps_data` SET `new_neighbour` = ? WHERE `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                StringBuilder ct = new StringBuilder();
                ct.append(map.getNewNeighbour()[0] == null ? -1 : map.getNewNeighbour()[0].getMapid() + ":"+map.getNewNeighbour()[0].getCellid()).append(",");
                ct.append(map.getNewNeighbour()[1] == null ? -1 : map.getNewNeighbour()[1].getMapid() + ":"+map.getNewNeighbour()[1].getCellid()).append(",");
                ct.append(map.getNewNeighbour()[2] == null ? -1 : map.getNewNeighbour()[2].getMapid() + ":"+map.getNewNeighbour()[2].getCellid()).append(",");
                ct.append(map.getNewNeighbour()[3] == null ? -1 : map.getNewNeighbour()[3].getMapid() + ":"+map.getNewNeighbour()[3].getCellid());
                pStatement.setString(1, ct.toString());
                pStatement.setInt(2, map.getId());
                pStatement.executeUpdate();
                logger.info(pStatement.toString());

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void insert(int map, short cell, DofusTrigger tg) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `maps_triggers` VALUES (?,?,?,?,?,?);")) {
                PreparedStatement pStatement = conn.getStatement();


                pStatement.setInt(1, 0);
                pStatement.setInt(2, map);
                pStatement.setShort(3, cell);
                pStatement.setInt(4, tg.getNewMap());
                pStatement.setInt(5, tg.getNewCell());
                pStatement.setString(6, "");
                pStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
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



    private int loadAllDoors() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_interactive_doors", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).addDoor(new MapDoor(result));
                } catch (Exception e) {
                   logger.debug("map {} door nulled",result.getInt("map"));
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllFightActions() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_endfight", 0)) {
            final ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).addAction(new MapAction(result.getByte("type"),result.getString("param")));
                } catch (Exception e) {
                    logger.debug("map {} endfight nulled", result.getInt("map"));
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
    public void insertMapAction(int map, MapAction door) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `map_endfight` VALUES (?,?,?);")) {
                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, map);
                pStatement.setByte(2, door.getAction());
                pStatement.setString(3, Enumerable.join(door.getParam()));
                pStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void updateDoor(MapDoor map) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `maps_interactive_doors` SET `type` = ?,`parameters` = ? WHERE `elem_id` = ? AND `map` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, map.getType());
                pStatement.setString(2, map.getParameters());
                pStatement.setInt(3, map.getElementID());
                pStatement.setInt(4, map.getMap());
                pStatement.executeUpdate();

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }


    @Override
    public void insertDoor(MapDoor door) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `maps_interactive_doors` VALUES (?,?,?,?,?);")) {
                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, door.getElementID());
                pStatement.setInt(2, door.getMap());
                pStatement.setInt(3, door.getType());
                pStatement.setString(4, door.getParameters());
                pStatement.setString(5, null);
                pStatement.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
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
                    logger.debug("map {} stated nulled",result.getInt("map"));
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
        return dofusMaps.values().stream()
                .filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y)
                .findFirst()
                .orElse(null);
    }

    @Override
    public MapPosition[] getSubAreaOfPos(int X, int Y) {
        return dofusMaps.values().stream()
                .filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y)
                .map(x -> x.getPosition())
                .toArray(MapPosition[]::new);
    }

    @Override
    public DofusMap findMapByPos(int X, int Y, int subArea) {
        return dofusMaps.values().stream()
                .filter(x -> x.getPosition() != null && x.getPosition().getPosX() == X && x.getPosition().getPosY() == Y && x.getPosition().getSubAreaId() == subArea).
                        findFirst()
                .orElse(null);

    }

    private int loadAllPositions() {
        int i = 0, i2 = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from map_positions", 0)) {
            final ResultSet result = conn.getResult();


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
            final ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    dofusMaps.get(result.getInt("map")).getInteractiveElements().add(new InteractiveElementStruct(result.getInt("element_id"), result.getInt("element_type_id"), result.getString("enabled_skills"), result.getString("disabled_skills"), result.getShort("age_bonus")));
                } catch (Exception e) {
                    logger.debug("map {} element nulled", result.getInt("map"));
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
        logger.info("Loaded {} end fight actions", this.loadAllFightActions());
        logger.info("Loaded {} map positions", this.loadAllPositions());
        logger.info("Loaded {} map zaaps", this.loadAllZaaps());
        logger.info("Loaded {} map subways", this.loadAllSubways());

    }

    @Override
    public void stop() {

    }
}
