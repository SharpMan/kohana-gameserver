package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SubAreaCriterion extends Criterion {

    public static String Identifier = "PB";
    public Integer subArea;

    @Override
    public String toString() {
        return this.FormatToString("PB");
    }

    @Override
    public void Build() {
        this.subArea = Integer.parseInt(Literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.currentMap.getSubAreaId(), this.subArea);
    }
}
