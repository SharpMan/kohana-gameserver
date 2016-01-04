package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class JobCriterion extends Criterion {

    public static String Identifier = "PJ";
    public static String Identifier2 = "Pj";

    public int id;
    public int level;

    @Override
    public String toString() {
        return this.FormatToString("PJ");
    }

    @Override
    public void Build() {
        if (this.literal.contains(",")) {
            this.id = Integer.parseInt(literal.split(",")[0]);
            this.level = Integer.parseInt(literal.split(",")[1]);
        } else {
            this.id = Integer.parseInt(literal);
            this.level = -1;
        }
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
