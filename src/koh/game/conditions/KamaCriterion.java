package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class KamaCriterion extends Criterion {

    public static final String IDENTIFIER = "PK";
    public Integer kamas;

    @Override
    public String toString() {
        return this.FormatToString("PK");
    }

    @Override
    public void Build() {
        this.kamas = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getKamas(), this.kamas);
    }

}
