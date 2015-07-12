package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class PvpRankCriterion extends Criterion {

    public static String Identifier = "PP";
    public static String Identifier2 = "Pp";

    public Byte Rank;

    @Override
    public String toString() {
        return this.FormatToString("PP");
    }

    @Override
    public void Build() {
        Rank = Byte.parseByte(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Byte>) character.AlignmentGrade, this.Rank);
    }
}
