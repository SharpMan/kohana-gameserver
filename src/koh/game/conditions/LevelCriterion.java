package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class LevelCriterion extends Criterion {

    public static String Identifier = "PL";
    public Integer Level;

    @Override
    public String toString() {
        return this.FormatToString("PL");
    }

    @Override
    public void Build() {
        this.Level = Integer.parseInt(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Integer>) character.level, this.Level);
    }

}
