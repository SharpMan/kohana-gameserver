package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class FriendListCriterion extends Criterion {

    public static String Identifier = "Pb";

    public int Friend;

    @Override
    public String toString() {
        return this.FormatToString("Pb");
    }

    @Override
    public void Build() {
        this.Friend = Integer.parseInt(Literal);
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
