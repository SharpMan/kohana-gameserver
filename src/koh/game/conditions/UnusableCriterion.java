package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class UnusableCriterion extends Criterion {

    public static String Identifier = "BI";

    @Override
    public String toString() {
        return this.FormatToString("BI");
    }

    @Override
    public void Build() {
        
    }

    @Override
    public boolean Eval(Player character) {
       return false;
    }

}
