package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SkillCriterion extends Criterion {

    public static String Identifier = "Pi";
    public static String Identifier2 = "PI";

    @Override
    public String toString() {
        return this.FormatToString("PL");
    }

    @Override
    public void Build() {
        
    }

    @Override
    public boolean Eval(Player character) {
        return true;
    }
}
