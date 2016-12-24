package koh.game.conditions;

import koh.game.entities.actors.Player;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Melancholia on 12/9/16.
 */
public class GuildRightCriterion extends Criterion {

    public static final String IDENTIFIER = "Px";
    public Integer m;

    @Override
    public String toString() {
        return this.FormatToString("Px");
    }

    @Override
    public void Build() {
        this.m = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        if(character.getGuildMember() != null && character.getGuildMember().hasRight(m)){
            return true;
        }
        return false;
    }
}

