package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class JobCriterion extends Criterion {

    public static String Identifier = "PJ";
    public static String Identifier2 = "Pj";

    public int Id;
    public int Level;

    @Override
    public String toString() {
        return this.FormatToString("PJ");
    }

    @Override
    public void Build() {
        if (this.literal.contains(",")) {
            System.out.println(literal);
            this.Id = Integer.parseInt(literal.split(",")[0]);
            this.Level = Integer.parseInt(literal.split(",")[1]);
        } else {
            this.Id = Integer.parseInt(literal);
            this.Level = -1;
        }
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
