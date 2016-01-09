package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class LevelCriterion extends Criterion {

    public static String Identifier = "PL";
    public Integer level;

    @Override
    public String toString() {
        return this.FormatToString("PL");
    }

    @Override
    public void Build() {
        this.level = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getLevel(), this.level);
    }

}
