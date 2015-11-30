package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ExpDAO;
import koh.game.entities.ExpLevel;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.TabMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ExpDAOImpl extends ExpDAO {

    private static final Logger logger = LogManager.getLogger(ExpDAOImpl.class);

    @Inject
    private DatabaseSource dbSource;

    private TabMap<ExpLevel> ExpLevels;
    private int maxLevel;

    private void computeMaxLevel() {
        for (ExpLevel e : ExpLevels.toTab())
            if (e.level > maxLevel)
                maxLevel = e.level;
    }

    private ArrayList<ExpLevel> loadExp() {
        ArrayList<ExpLevel> levels = new ArrayList<>();
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from experiences", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                levels.add(new ExpLevel(result.getInt("level"), result.getLong("player"), result.getLong("job"), result.getLong("mount"), result.getLong("guild"), result.getInt("guild_members"), result.getInt("living_object"), result.getInt("pvp_alignement"), result.getLong("tourmentors"), result.getLong("bandits")));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return levels;
    }

    private void loadAll() {
        ArrayList<ExpLevel> arr_levels = loadExp();
        ExpLevels = new TabMap<>(ExpLevel.class, arr_levels.size(), 1);
        for (ExpLevel lev : arr_levels) {
            ExpLevels.add(lev.level, lev);
        }
        arr_levels.clear();
        computeMaxLevel();
    }

    @Override
    public ExpLevel getLevel(int level) {
        if (level > getMaxLevel())
            level = getMaxLevel();

        if (level < 1)
            level = 1;

        return ExpLevels.get(level);
    }

    @Override
    public long getPlayerMinExp(int level) {
        if (level > getMaxLevel())
            level = getMaxLevel();

        if (level < 1)
            level = 1;

        return ExpLevels.get(level).player;
    }

    @Override
    public long getPlayerMaxExp(int level) {
        if (level >= getMaxLevel())
            level = (getMaxLevel() - 1);

        if (level <= 1)
            level = 1;

        return ExpLevels.get(++level).player;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public void start() {
        this.loadAll();
    }

    @Override
    public void stop() {

    }
}
