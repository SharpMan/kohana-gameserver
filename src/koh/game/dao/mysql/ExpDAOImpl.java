package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.google.inject.Inject;
import koh.game.MySQL;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ExpDAO;
import koh.game.entities.ExpLevel;
import koh.game.utils.Settings;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.TabMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ExpDAOImpl extends ExpDAO{

    private static final Logger logger = LogManager.getLogger(ExpDAOImpl.class);

    @Inject
    private DatabaseSource dbSource;

    private TabMap<ExpLevel> ExpLevels;
    private int maxLEVEL;

    //TODO : MaxLevel of each column
    private void inst_maxLevel() {
        for (ExpLevel e : ExpLevels.toTab()) {
            if (e.level > maxLEVEL) {
                maxLEVEL = e.level;
            }
        }
    }

    private void load_ExpLevels() {
        ArrayList<ExpLevel> arr_levels = loadExp();
        ExpLevels = new TabMap(ExpLevel.class, arr_levels.size(), 1);
        for (ExpLevel lev : arr_levels) {
            ExpLevels.add(lev.level, lev);
        }
        arr_levels.clear();
        inst_maxLevel();
    }

    public ExpLevel getFloorByLevel(int _lvl) {
        if (_lvl > getExpLevelSize()) {
            _lvl = getExpLevelSize();
        }
        if (_lvl < 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl);
    }

    public long persoXpMin(int _lvl) {
        if (_lvl > getExpLevelSize()) {
            _lvl = getExpLevelSize();
        }
        if (_lvl < 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl).Player;
    }

    public long persoXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize()) {
            _lvl = (getExpLevelSize() - 1);
        }
        if (_lvl <= 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl + 1).Player;
    }

    public int getExpLevelSize() {
        return maxLEVEL;
    }

    public ArrayList<ExpLevel> loadExp() {
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

    @Override
    public void start() {
        this.load_ExpLevels();
    }

    @Override
    public void stop() {

    }
}
