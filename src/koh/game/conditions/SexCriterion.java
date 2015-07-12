package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SexCriterion extends Criterion {

    public static String Identifier = "PS";
    public Integer Sex;

    @Override
    public String toString() {
        return this.FormatToString("PS");
    }

    @Override
    public void Build() {
       this.Sex = Integer.parseInt(Literal);
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Integer>) character.Sexe, this.Sex);
    }
}
