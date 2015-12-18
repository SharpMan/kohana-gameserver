package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class QuestActiveCriterion extends Criterion {

    public static String Identifier = "Qa";
    public Integer QuestId;

    @Override
    public String toString() {
        return this.FormatToString("Qa");
    }

    @Override
    public void Build() {
        this.QuestId = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
