package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MariedCriterion extends Criterion {

    public static String Identifier = "PR";
    public boolean Married;

    @Override
    public String toString() {
        return this.FormatToString("PR");
    }

    @Override
    public void Build() {
       this.Married = Integer.parseInt(Literal) == 1;
    }

    @Override
    public boolean Eval(Player character) {
         return true;
    }

}
