package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class BoolOperator extends ConditionExpression {

    public ConditionExpression Left;

    public ConditionExpression Right;

    public BoolOperatorEnum Operator;

    public BoolOperator(ConditionExpression left, ConditionExpression right, BoolOperatorEnum operator) {
        this.Left = left;
        this.Right = right;
        this.Operator = operator;
    }

    public static BoolOperatorEnum TryGetOperator(char c) {
        switch (c) {
            case '&':
                return BoolOperatorEnum.AND;
            case '|':
                return BoolOperatorEnum.OR;
            default:
                return null;
        }
    }

    public static char GetOperatorChar(BoolOperatorEnum op) {
        switch (op) {
            case AND:
                return '&';
            case OR:
                return '|';
            default:
                throw new Error(String.format("{0} is not a valid bool operator", op));
        }
    }

    @Override
    public boolean Eval(Player character) {
        if (this.Operator != BoolOperatorEnum.AND && this.Operator != BoolOperatorEnum.OR) {
            throw new Error(String.format("Cannot evaluate {0} : illegal bool operator {1}", this, this.Operator));
        }
        boolean flag1 = this.Left.Eval(character);
        if (this.Operator == BoolOperatorEnum.AND && !flag1) {
            return false;
        }
        if (this.Operator == BoolOperatorEnum.OR && flag1) {
            return true;
        }
        boolean flag2 = this.Right.Eval(character);
        if (this.Operator == BoolOperatorEnum.AND) {
            return flag1 && flag2;
        }
        if (this.Operator == BoolOperatorEnum.OR) {
            return flag1 || flag2;
        } else {
            throw new Error(String.format("Cannot evaluate {0}", this));
        }
    }

    @Override
    public String toString() {
        return String.format("{0}{1}{2}", this.Left, BoolOperator.GetOperatorChar(this.Operator), this.Right);
    }

}
