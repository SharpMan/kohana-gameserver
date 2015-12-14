package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class KamaCriterion extends Criterion {

    public static String Identifier = "PK";
    public Integer Kamas;

    @Override
    public String toString() {
        return this.FormatToString("PK");
    }

    @Override
    public void Build() {
        this.Kamas = Integer.parseInt(Literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.kamas, this.Kamas);
    }

}
