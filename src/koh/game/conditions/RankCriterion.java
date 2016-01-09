package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class RankCriterion extends Criterion {

    public static String Identifier = "Pq";
    public Integer Rank;

    @Override
    public String toString() {
        return this.FormatToString("Pq");
    }

    @Override
    public void Build() {
        this.Rank = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
       return false;
    }
}
