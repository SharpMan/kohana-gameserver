package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import koh.game.MySQL;
import koh.game.entities.ExpLevel;
import koh.game.utils.Settings;
import koh.utils.TabMap;

/**
 *
 * @author Neo-Craft
 */
public class ExpDAO {

    private static TabMap<ExpLevel> ExpLevels;
    public static int maxLEVEL;

    //TODO : MaxLevel of each column
    private static void inst_maxLevel() {
        for (ExpLevel e : ExpLevels.toTab()) {
            if (e.level > maxLEVEL) {
                maxLEVEL = e.level;
            }
        }
    }

    public static void load_ExpLevels() {
        ArrayList<ExpLevel> arr_levels = LOAD_EXP();
        ExpLevels = new TabMap(ExpLevel.class, arr_levels.size(), 1);
        for (ExpLevel lev : arr_levels) {
            ExpLevels.add(lev.level, lev);
        }
        arr_levels.clear();
        inst_maxLevel();
    }

    public static ExpLevel GetFloorByLevel(int _lvl) {
        if (_lvl > getExpLevelSize()) {
            _lvl = getExpLevelSize();
        }
        if (_lvl < 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl);
    }

    public static long PersoXpMin(int _lvl) {
        if (_lvl > getExpLevelSize()) {
            _lvl = getExpLevelSize();
        }
        if (_lvl < 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl).Player;
    }

    public static long PersoXpMax(int _lvl) {
        if (_lvl >= getExpLevelSize()) {
            _lvl = (getExpLevelSize() - 1);
        }
        if (_lvl <= 1) {
            _lvl = 1;
        }
        return ExpLevels.get(_lvl + 1).Player;
    }

    public static int getExpLevelSize() {
        return maxLEVEL;
    }

    public static ArrayList<ExpLevel> LOAD_EXP() {
        ArrayList<ExpLevel> levels = new ArrayList<>();
        try {
            ResultSet RS = MySQL.executeQuery("SELECT * from experiences;", Settings.GetStringElement("Database.Name"));
            while (RS.next()) {
                levels.add(new ExpLevel(RS.getInt("level"), RS.getLong("player"), RS.getLong("job"), RS.getLong("mount"), RS.getLong("guild"), RS.getInt("guild_members"), RS.getInt("living_object"), RS.getInt("pvp_alignement"), RS.getLong("tourmentors"), RS.getLong("bandits")));
            }
            MySQL.closeResultSet(RS);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return levels;
    }

}
