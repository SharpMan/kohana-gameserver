package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.google.inject.Inject;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AreaDAO;
import koh.game.entities.environments.Area;
import koh.game.entities.environments.SubArea;
import koh.game.entities.environments.SuperArea;
import koh.game.utils.Settings;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class AreaDAOImpl extends AreaDAO {

    private static final Logger logger = LogManager.getLogger(AreaDAO.class);

    private final HashMap<Integer, SuperArea> superAreas = new HashMap<>(5);
    private final HashMap<Integer, Area> areas = new HashMap<>(56);
    private final HashMap<Integer, SubArea> subAreas = new HashMap<>(855);

    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from areas", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                areas.put(result.getInt("id"), new Area() {
                    {
                        this.id = result.getInt("id");
                        this.superArea = superAreas.get(result.getInt("super_area"));
                        this.containHouses = result.getBoolean("contain_houses");
                        this.containPaddocks = result.getBoolean("contain_paddocks");
                        this.worldmapId = result.getInt("world_map_id");
                        this.hasWorldMap = result.getBoolean("has_world_map");
                        superAreas.get(result.getInt("super_area")).Areas = ArrayUtils.add(superAreas.get(result.getInt("super_area")).Areas, this);
                    }
                });

                ++i;
            }
        } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllSubAreas() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from sub_area", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                subAreas.put(result.getInt("id"), new SubArea() {
                    {
                        this.Id = result.getInt("id");
                        this.area = areas.get(result.getInt("area_id"));
                        this.mapIds = Enumerable.StringToIntArray(result.getString("map_ids"));
                        this.shape = Enumerable.StringToIntArray(result.getString("shape"));
                        this.customWorldMaptype = Enumerable.StringToIntArray(result.getString("custom_world_map"));
                        this.packId = result.getInt("pack_id");
                        this.level = result.getInt("level");
                        this.isConquestVillage = result.getBoolean("is_conquest_village");
                        this.basicAccountAllowed = result.getBoolean("basic_account_allowed");
                        this.displayOnWorldMap = result.getBoolean("display_on_world_map");
                        this.monsters = Enumerable.StringToIntArray(result.getString("monsters"));
                        this.entranceMapIds = Enumerable.StringToIntArray(result.getString("entrance_map_ids"));
                        this.exitMapIds = Enumerable.StringToIntArray(result.getString("exit_map_ids"));
                        this.capturable = result.getBoolean("capturable");
                        areas.get(result.getInt("area_id")).SubAreas = ArrayUtils.add(areas.get(result.getInt("area_id")).SubAreas, this);
                    }
                });

                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllSuper() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from super_areas", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                superAreas.put(result.getInt("id"), new SuperArea() {
                    {
                        this.Id = result.getInt("id");
                        this.worldmapIdtype = result.getInt("world_map_id");
                        this.hasWorldMaptype = result.getBoolean("has_world_map");
                    }
                });

                ++i;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        logger.info("Loaded {} super areas", this.loadAllSuper());
        logger.info("Loaded {} areas", this.loadAll());
        logger.info("Loaded {} sub areas", this.loadAllSubAreas());
    }

    @Override
    public void stop() {

    }

    @Override
    public SuperArea getSuperArea(int id) {
        return superAreas.get(id);
    }

    @Override
    public Area getArea(int id) {
        return areas.get(id);
    }

    @Override
    public SubArea getSubArea(int id) {
        return subAreas.get(id);
    }
}
