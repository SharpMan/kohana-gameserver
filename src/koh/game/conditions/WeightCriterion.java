package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class WeightCriterion extends Criterion {

    public static String Identifier = "PW";
    public Integer Weight;

    @Override
    public String toString() {
        return this.FormatToString("PW");
    }

    @Override
    public void Build() {
        this.Weight = Integer.parseInt(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Integer>) character.inventoryCache.getWeight(), this.Weight);
    }
}
