package koh.game.conditions;

import java.util.Calendar;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MonthCriterion extends Criterion {

    public static String Identifier = "SG";
    public Integer Month;

    @Override
    public String toString() {
        return this.FormatToString("SG");
    }

    @Override
    public void Build() {
         this.Month = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) Calendar.getInstance().MONTH, this.Month);
    }
}
