package koh.game.entities.mob;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MonsterDrop {

    public int dropId;
    public int monsterId;
    public int objectId;
    public double percentDropForGrade1;
    public double percentDropForGrade2;
    public double percentDropForGrade3;
    public double percentDropForGrade4;
    public double percentDropForGrade5;
    public int DropLimit, ProspectingLock;
    public boolean hasCriteria;
    public String criteria;

    public MonsterDrop(ResultSet result) throws SQLException {
        dropId = result.getInt("drop_id");
        monsterId = result.getInt("monster_id");
        objectId = result.getInt("monster_id");
        percentDropForGrade1 = result.getDouble("percent_drop_for_grade1");
        percentDropForGrade2 = result.getDouble("percent_drop_for_grade2");
        percentDropForGrade3 = result.getDouble("percent_drop_for_grade3");
        percentDropForGrade4 = result.getDouble("percent_drop_for_grade4");
        percentDropForGrade5 = result.getDouble("percent_drop_for_grade5");
        DropLimit = result.getInt("drop_limit");
        ProspectingLock = result.getInt("prospecting_lock");
        hasCriteria = result.getBoolean("has_criteria");
        criteria = result.getString("criteria");
    }
}
