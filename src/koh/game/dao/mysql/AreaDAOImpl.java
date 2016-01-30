package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AreaDAO;
import koh.game.entities.environments.Area;
import koh.game.entities.environments.SubArea;
import koh.game.entities.environments.SuperArea;
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
    private final HashMap<Integer, Area> areas = new HashMap<>(900);
    private final HashMap<Integer, SubArea> subAreas = new HashMap<>(855);

    @Inject
    private DatabaseSource dbSource;

    @Override
    public Collection<SubArea> getSubAreas(){
        return this.subAreas.values();
    }

    private int loadAll() {
        int i = 0;

        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from areas", 0)) {
            ResultSet result = conn.getResult();

            Area myArea = null;
            while (result.next()) {
                myArea = Area.builder()
                        .id(result.getInt("id"))
                        .superArea(superAreas.get(result.getInt("super_area")))
                        .containHouses(result.getBoolean("contain_houses"))
                        .containPaddocks(result.getBoolean("contain_paddocks"))
                        .worldmapId(result.getInt("world_map_id"))
                        .hasWorldMap(result.getBoolean("has_world_map"))
                        .build();
                myArea.getSuperArea().setAreas(ArrayUtils.add(myArea.getSuperArea().getAreas(), myArea));
                myArea.onBuilt();
                areas.put(result.getInt("id"), myArea);

                ++i;
            }
        } catch (Exception e) {
                e.printStackTrace();
        }
        return i;
    }

    private int loadAllSubAreas() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from sub_areas", 0)) {
            ResultSet result = conn.getResult();
            SubArea mySubarea = null;
            while (result.next()) {
                mySubarea = SubArea.builder()
                        .id(result.getInt("id"))
                        .area(areas.get(result.getInt("area_id")))
                        .mapIds(Enumerable.stringToIntArray(result.getString("map_ids")))
                        .shape(Enumerable.stringToIntArray(result.getString("shape")))
                        .customWorldMaptype(Enumerable.stringToIntArray(result.getString("custom_world_map")))
                        .packId(result.getInt("pack_id"))
                        .level(result.getInt("level"))
                        .isConquestVillage(result.getBoolean("is_conquest_village"))
                        .basicAccountAllowed(result.getBoolean("basic_account_allowed"))
                        .displayOnWorldMap(result.getBoolean("display_on_world_map"))
                        .monsters(Enumerable.stringToIntArray(result.getString("monsters")))
                        .entranceMapIds(Enumerable.stringToIntArray(result.getString("entrance_map_ids")))
                        .exitMapIds(Enumerable.stringToIntArray(result.getString("exit_map_ids")))
                        .capturable(result.getBoolean("capturable"))
                        .build();

                mySubarea.removeArchiMonster();
                mySubarea.getArea().getSubAreas().add(mySubarea);
                subAreas.put(result.getInt("id"), mySubarea);

                ++i;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllSuper() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from super_areas", 0)) {
            ResultSet result = conn.getResult();
            SuperArea klass;
            while (result.next()) {
                klass = SuperArea.builder()
                        .id(result.getInt("id"))
                        .worldMapId(result.getInt("world_map_id"))
                        .hasWorldMaptype(result.getBoolean("has_world_map"))
                        .build();
                superAreas.put(result.getInt("id"), klass);

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
