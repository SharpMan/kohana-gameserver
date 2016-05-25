package koh.game.conditions;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Stack;

/**
 *
 * @author Neo-Craft
 */
public class ConditionParser {

    private final HashMap<Integer, Integer> m_parentheses = new HashMap<>();

    public StringBuffer str;

    public ConditionParser(String s) {
        this.str = new StringBuffer(s);
    }

    public ConditionExpression parse() {
        this.trimAllSpaces();
        PriorityOperator priorityOperator = null;
        this.parseParentheses();
        while (this.m_parentheses.containsKey(0) && this.m_parentheses.get(0) == this.str.length() - 1) {
            this.str = this.str.deleteCharAt(this.str.length() - 1).deleteCharAt(0);
            if (priorityOperator != null) {
                priorityOperator.Expression =  new PriorityOperator();
                priorityOperator = (PriorityOperator) priorityOperator.Expression;
            } else {
                priorityOperator = new PriorityOperator();
            }
            this.parseParentheses();
        }
        final ConditionExpression conditionExpression1 = this.tryParseBoolOperator();
        if (conditionExpression1 != null) {
            if (priorityOperator == null) {
                return conditionExpression1;
            }
            priorityOperator.Expression = conditionExpression1;
            return (ConditionExpression) priorityOperator;
        } else {
            final ConditionExpression conditionExpression2 = this.tryParseComparaisonOperator();
            if (conditionExpression2 == null) {
                throw new Error(String.format("Cannot parse {0} : No operator found", this.str));
            }
            if (priorityOperator == null) {
                return conditionExpression2;
            }
            priorityOperator.Expression = conditionExpression2;
            return (ConditionExpression) priorityOperator;
        }
    }

    private ConditionExpression tryParseBoolOperator() {
        boolean flag1 = false;
        boolean flag2 = false;
        for (int length = 0; length < this.str.length(); ++length) {
            if ((int) this.str.charAt(length) == 34) {
                flag1 = !flag1;
            } else if ((int) this.str.charAt(length) == 40) {
                flag2 = true;
            } else if ((int) this.str.charAt(length) == 41) {
                flag2 = false;
            } else if (!flag1 && !flag2) {
                BoolOperatorEnum operator = BoolOperator.TryGetOperator(this.str.charAt(length));
                if (operator != null) {
                    if (length + 1 >= this.str.length()) {
                        throw new Error(String.format("Cannot parse {0} :  right Expression of bool operator index {1} is empty", this.str, length));
                    }
                    String s1 = this.str.substring(0, length);
                    if (Strings.isNullOrEmpty(s1)) {
                        throw new Error(String.format("Cannot parse {0} : left Expression of bool operator index {1} is empty", this.str, length));
                    }
                    ConditionExpression left = new ConditionParser(s1).parse();
                    String s2 = this.str.toString().substring(length + 1, (length + 1)+this.str.length() - (length + 1));
                    if (Strings.isNullOrEmpty(s2)) {
                        throw new Error(String.format("Cannot parse {0} : right Expression of bool operator index {1} is empty", this.str, length));
                    }
                    ConditionExpression right = new ConditionParser(s2).parse();
                    return (ConditionExpression) new BoolOperator(left, right, operator);
                }
            }
        }
        return (ConditionExpression) null;
    }

    private void trimAllSpaces() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = false;
        for (int index = 0; index < this.str.length(); ++index) {
            if ((int) this.str.charAt(index) == 34) {
                flag = !flag;
            }
            if (flag) {
                stringBuilder.append(this.str.charAt(index));
            } else if ((int) this.str.charAt(index) != 32) {
                stringBuilder.append(this.str.charAt(index));
            }
        }
        this.str = new StringBuffer((stringBuilder).toString());
    }

    private void parseParentheses() {
        this.m_parentheses.clear();
        final Stack<Integer> stack = new Stack<>();
        for (int index = 0; index < this.str.length(); index++) {
            if (this.str.charAt(index) == '(') {
                stack.push(index);
            }
            if (this.str.charAt(index) == ')' && stack.size() > 0) {
                this.m_parentheses.put(stack.pop(), index);
            } else if (this.str.charAt(index) == ')' && stack.size() <= 0) {
                throw new Error(String.format("Cannot evaluate {0} : Parenthese at index {1} is not binded to an open parenthese", this.str, index));
            }
        }
        if (stack.size() > 0) {
            throw new Error(String.format("Cannot evaluate {0} : Parenthese at index {1} is not closed", this.str, stack.pop()));
        }
    }

    private ConditionExpression tryParseComparaisonOperator() {
        int length = 0;
        boolean flag1 = false;
        boolean flag2 = false;
        while (length < this.str.length()) {
            if ((int) this.str.charAt(length) == 34) {
                flag1 = !flag1;
                ++length;
            } else if ((int) this.str.charAt(length) == 40) {
                flag2 = true;
                ++length;
            } else if ((int) this.str.charAt(length) == 41) {
                flag2 = false;
                ++length;
            } else if (flag1 || flag2) {
                ++length;
            } else {
                ComparaisonOperatorEnum operator = Criterion.TryGetOperator(this.str.charAt(length));
                if (operator != null) {
                    if (length + 1 >= this.str.length()) {
                        throw new Error(String.format("Cannot parse {0} :  right Expression of comparaison operator index {1} is empty", this.str, length));
                    }
                    String name = this.str.toString().substring(0, length);
                    if (Strings.isNullOrEmpty(name)) {
                        throw new Error(String.format("Cannot parse {0} : left Expression of comparaison operator index {1} is empty", this.str, length));
                    }
                    final Criterion criterionByName = Criterion.CreateCriterionByName(name);
                    String str = this.str.toString().substring(length + 1, (length + 1)+this.str.length() - (length + 1));
                    
                    if (Strings.isNullOrEmpty(str)) {
                        throw new Error(String.format("Cannot parse {0} : right Expression of comparaison operator index {1} is empty", this.str, length));
                    }
                    criterionByName.literal = str;
                    criterionByName.operator = operator;
                    criterionByName.Build();
                    return (ConditionExpression) criterionByName;
                } else {
                    ++length;
                }
            }
        }
        return (ConditionExpression) null;
    }

}
