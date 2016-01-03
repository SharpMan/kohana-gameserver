package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MonsterDAO;
import koh.game.entities.mob.MonsterDrop;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Neo-Craft
 */
public class MonsterDAOImpl extends MonsterDAO {

    private static final Logger logger = LogManager.getLogger(MonsterDAOImpl.class);
    private final HashMap<Integer, MonsterTemplate> templates = new HashMap<>(3000);
    @Inject
    private DatabaseSource dbSource;


    @Override
    public MonsterTemplate find(int id){
        return this.templates.get(id);
    }

    private int loadAll() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.put(result.getInt("id"), new MonsterTemplate(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return templates.size();
    }

    private int loadAllDrops() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_drops", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                 templates.get(result.getInt("monster_id")).getDrops().add(new MonsterDrop(result));
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllGrades() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_grades ORDER by grade ASC", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.get(result.getInt("monster_id")).getGrades().add(new MonsterGrade(result));
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        logger.info("Loaded {} template monster", this.loadAll());
        logger.info("Loaded {} template grades", this.loadAllGrades());
        logger.info("Loaded {} template drops", this.loadAllDrops());
    }

    @Override
    public void stop() {

    }
}
