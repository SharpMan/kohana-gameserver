package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class RideCriterion extends Criterion {

    public static String Identifier = "Pf";
    public boolean Mounted;

    @Override
    public String toString() {
        return this.FormatToString("Pf");
    }

    @Override
    public void Build() {
        this.Mounted = Integer.parseInt(Literal) != 0;
    }

    @Override
    public boolean Eval(Player character) {
        return true;
    }
}
