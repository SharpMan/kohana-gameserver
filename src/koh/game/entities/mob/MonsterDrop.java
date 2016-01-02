package koh.game.entities.mob;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MonsterDrop {

    @Getter
    private int dropId,monsterId,objectId;
    @Getter
    private double percentDropForGrade1,percentDropForGrade2;
    @Getter
    private double percentDropForGrade3,percentDropForGrade4,percentDropForGrade5;
    @Getter
    public int dropLimit, prospectingLock;
    @Getter
    private boolean hasCriteria;
    private String criteria;

    public MonsterDrop(ResultSet result) throws SQLException {
        dropId = result.getInt("drop_id");
        monsterId = result.getInt("monster_id");
        objectId = result.getInt("object_id");
        percentDropForGrade1 = result.getDouble("percent_drop_for_grade1");
        percentDropForGrade2 = result.getDouble("percent_drop_for_grade2");
        percentDropForGrade3 = result.getDouble("percent_drop_for_grade3");
        percentDropForGrade4 = result.getDouble("percent_drop_for_grade4");
        percentDropForGrade5 = result.getDouble("percent_drop_for_grade5");
        dropLimit = result.getInt("drop_limit");
        prospectingLock = result.getInt("prospecting_lock");
        hasCriteria = result.getBoolean("has_criteria");
        criteria = result.getString("criteria");
    }

    public double getDropRate(int grade)
    {
        double result;
        if (grade <= 1)
        {
            result = this.percentDropForGrade1;
        }
        else
        {
            if (grade == 2)
            {
                result = this.percentDropForGrade2;
            }
            else
            {
                if (grade == 3)
                {
                    result = this.percentDropForGrade3;
                }
                else
                {
                    if (grade == 4)
                    {
                        result = this.percentDropForGrade4;
                    }
                    else
                    {
                        result = this.percentDropForGrade5;
                    }
                }
            }
        }
        return result;
    }
}

