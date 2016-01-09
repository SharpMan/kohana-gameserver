package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SexCriterion extends Criterion {

    public static String Identifier = "PS";
    public Integer sex;

    @Override
    public String toString() {
        return this.FormatToString("PS");
    }

    @Override
    public void Build() {
       this.sex = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Integer>) character.getSexe(), this.sex);
    }
}
