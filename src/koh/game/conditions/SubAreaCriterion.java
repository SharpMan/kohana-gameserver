package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SubAreaCriterion extends Criterion {

    public static final String IDENTIFIER = "PB";
    public Integer subArea;

    @Override
    public String toString() {
        return this.FormatToString("PB");
    }

    @Override
    public void Build() {
        this.subArea = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getCurrentMap().getSubAreaId(), this.subArea);
    }
}
