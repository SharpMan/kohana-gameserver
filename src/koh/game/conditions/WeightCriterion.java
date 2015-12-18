package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class WeightCriterion extends Criterion {

    public static final String IDENTIFIER = "PW";
    public Integer weight;

    @Override
    public String toString() {
        return this.FormatToString("PW");
    }

    @Override
    public void Build() {
        this.weight = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getInventoryCache().getWeight(), this.weight);
    }
}
