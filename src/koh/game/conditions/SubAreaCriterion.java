package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SubAreaCriterion extends Criterion {

    public static String Identifier = "PB";
    public Integer SubArea;

    @Override
    public String toString() {
        return this.FormatToString("PB");
    }

    @Override
    public void Build() {
        this.SubArea = Integer.parseInt(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Integer>) character.CurrentMap.SubAreaId, this.SubArea);
    }
}
