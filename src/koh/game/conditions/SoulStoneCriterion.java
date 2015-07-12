package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SoulStoneCriterion extends Criterion {

    public static String Identifier = "PA";

    @Override
    public String toString() {
        return this.FormatToString("PA");
    }

    @Override
    public void Build() {
        
    }

    @Override
    public boolean Eval(Player character) {
         return true;
    }
}
