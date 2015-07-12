package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public abstract class ConditionExpression {

    public static ConditionExpression Parse(String str) {
        return new ConditionParser(str).Parse();
    }

    public abstract boolean Eval(Player character);
}
