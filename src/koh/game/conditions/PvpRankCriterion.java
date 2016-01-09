package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class PvpRankCriterion extends Criterion {

    public static final String IDENTIFIER = "PP", IDENTIFIER_2 = "Pp";

    public Byte rank;

    @Override
    public String toString() {
        return this.FormatToString("PP");
    }

    @Override
    public void Build() {
        rank = Byte.parseByte(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Byte>) character.getAlignmentGrade(), this.rank);
    }
}
