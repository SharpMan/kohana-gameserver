package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class LevelCriterion extends Criterion {

    public static final String IDENTIIDENTIFIERIER = "PL";
    public Integer level;

    @Override
    public String toString() {
        return this.FormatToString("PL");
    }

    @Override
    public void Build() {
        this.level = Integer.parseInt(literal.trim());
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getLevel(), this.level);
    }

}
