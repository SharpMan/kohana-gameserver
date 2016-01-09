package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class SubscribeCriterion extends Criterion {

    public static String Identifier = "PZ";
    public boolean SubscriptionState;

    @Override
    public String toString() {
        return this.FormatToString("PZ");
    }

    @Override
    public void Build() {
        SubscriptionState = Integer.parseInt(literal) != 0;
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }
}
