package koh.game.conditions;

import koh.game.entities.actors.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Melancholia on 6/14/16.
 */
public class DayItemCriterion extends Criterion {

    public static final String IDENTIFIER = "Sd";
    public Integer day;

    @Override
    public String toString() {
        return this.FormatToString("Sd");
    }

    @Override
    public void Build() {
        this.day = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        final Calendar now = GregorianCalendar.getInstance();
        final int dayNumber = now.get(Calendar.DAY_OF_MONTH);
        return this.Compare((Comparable<Integer>) dayNumber, this.day);
    }
}
