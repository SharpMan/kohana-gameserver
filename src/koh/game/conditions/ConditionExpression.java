package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public abstract class ConditionExpression {

    public static ConditionExpression parse(String str) {
        return new ConditionParser(str).parse();
    }

    public abstract boolean eval(Player character);
}
