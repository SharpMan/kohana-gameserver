package koh.game.conditions;

import koh.game.entities.actors.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Melancholia on 6/14/16.
 */
public class MonthItemCriterion extends Criterion {

    public static final String IDENTIFIER = "SG";
    public Integer m;

    @Override
    public String toString() {
        return this.FormatToString("SG");
    }

    @Override
    public void Build() {
        this.m = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        final Calendar now = GregorianCalendar.getInstance();
        final int dayNumber = now.get(Calendar.MONTH) -1;
        return this.Compare((Comparable<Integer>) dayNumber, this.m);
    }
}
