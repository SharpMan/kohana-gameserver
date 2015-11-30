package koh.game.conditions;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Stack;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Neo-Craft
 */
public class ConditionParser {

    private HashMap<Integer, Integer> m_parentheses = new HashMap<>();

    public StringBuffer Str;

    public ConditionParser(String s) {
        this.Str = new StringBuffer(s);
    }

    public ConditionExpression Parse() {
        this.TrimAllSpaces();
        PriorityOperator priorityOperator = (PriorityOperator) null;
        this.ParseParentheses();
        while (this.m_parentheses.containsKey(0) && this.m_parentheses.get(0) == this.Str.length() - 1) {
            this.Str = this.Str.delete(this.Str.length() - 1, 1).delete(0, 1);
            if (priorityOperator != null) {
                priorityOperator.Expression = (ConditionExpression) new PriorityOperator();
                priorityOperator = (PriorityOperator) priorityOperator.Expression;
            } else {
                priorityOperator = new PriorityOperator();
            }
            this.ParseParentheses();
        }
        ConditionExpression conditionExpression1 = this.TryParseBoolOperator();
        if (conditionExpression1 != null) {
            if (priorityOperator == null) {
                return conditionExpression1;
            }
            priorityOperator.Expression = conditionExpression1;
            return (ConditionExpression) priorityOperator;
        } else {
            ConditionExpression conditionExpression2 = this.TryParseComparaisonOperator();
            if (conditionExpression2 == null) {
                throw new Error(String.format("Cannot parse {0} : No operator found", this.Str));
            }
            if (priorityOperator == null) {
                return conditionExpression2;
            }
            priorityOperator.Expression = conditionExpression2;
            return (ConditionExpression) priorityOperator;
        }
    }

    private ConditionExpression TryParseBoolOperator() {
        boolean flag1 = false;
        boolean flag2 = false;
        for (int length = 0; length < this.Str.length(); ++length) {
            if ((int) this.Str.charAt(length) == 34) {
                flag1 = !flag1;
            } else if ((int) this.Str.charAt(length) == 40) {
                flag2 = true;
            } else if ((int) this.Str.charAt(length) == 41) {
                flag2 = false;
            } else if (!flag1 && !flag2) {
                BoolOperatorEnum operator = BoolOperator.TryGetOperator(this.Str.charAt(length));
                if (operator != null) {
                    if (length + 1 >= this.Str.length()) {
                        throw new Error(String.format("Cannot parse {0} :  right Expression of bool operator index {1} is empty", this.Str, length));
                    }
                    String s1 = this.Str.substring(0, length);
                    if (Strings.isNullOrEmpty(s1)) {
                        throw new Error(String.format("Cannot parse {0} : Left Expression of bool operator index {1} is empty", this.Str, length));
                    }
                    ConditionExpression left = new ConditionParser(s1).Parse();
                    String s2 = this.Str.toString().substring(length + 1, (length + 1)+this.Str.length() - (length + 1));
                    if (Strings.isNullOrEmpty(s2)) {
                        throw new Error(String.format("Cannot parse {0} : right Expression of bool operator index {1} is empty", this.Str, length));
                    }
                    ConditionExpression right = new ConditionParser(s2).Parse();
                    return (ConditionExpression) new BoolOperator(left, right, operator);
                }
            }
        }
        return (ConditionExpression) null;
    }

    private void TrimAllSpaces() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = false;
        for (int index = 0; index < this.Str.length(); ++index) {
            if ((int) this.Str.charAt(index) == 34) {
                flag = !flag;
            }
            if (flag) {
                stringBuilder.append(this.Str.charAt(index));
            } else if ((int) this.Str.charAt(index) != 32) {
                stringBuilder.append(this.Str.charAt(index));
            }
        }
        this.Str = new StringBuffer((stringBuilder).toString());
    }

    private void ParseParentheses() {
        this.m_parentheses.clear();
        Stack<Integer> stack = new Stack<>();
        for (int index = 0; index < this.Str.length(); ++index) {
            if ((int) this.Str.charAt(index) == 40) {
                stack.push(index);
            }
            if ((int) this.Str.charAt(index) == 41 && stack.size() > 0) {
                this.m_parentheses.put(stack.pop(), index);
            } else if ((int) this.Str.charAt(index) == 41 && stack.size() <= 0) {
                throw new Error(String.format("Cannot evaluate {0} : Parenthese at index {1} is not binded to an open parenthese", this.Str, index));
            }
        }
        if (stack.size() > 0) {
            throw new Error(String.format("Cannot evaluate {0} : Parenthese at index {1} is not closed", this.Str, stack.pop()));
        }
    }

    private ConditionExpression TryParseComparaisonOperator() {
        int length = 0;
        boolean flag1 = false;
        boolean flag2 = false;
        while (length < this.Str.length()) {
            if ((int) this.Str.charAt(length) == 34) {
                flag1 = !flag1;
                ++length;
            } else if ((int) this.Str.charAt(length) == 40) {
                flag2 = true;
                ++length;
            } else if ((int) this.Str.charAt(length) == 41) {
                flag2 = false;
                ++length;
            } else if (flag1 || flag2) {
                ++length;
            } else {
                ComparaisonOperatorEnum operator = Criterion.TryGetOperator(this.Str.charAt(length));
                if (operator != null) {
                    if (length + 1 >= this.Str.length()) {
                        throw new Error(String.format("Cannot parse {0} :  right Expression of comparaison operator index {1} is empty", this.Str, length));
                    }
                    String name = this.Str.toString().substring(0, length);
                    if (Strings.isNullOrEmpty(name)) {
                        throw new Error(String.format("Cannot parse {0} : Left Expression of comparaison operator index {1} is empty", this.Str, length));
                    }
                    Criterion criterionByName = Criterion.CreateCriterionByName(name);
                    String str = this.Str.toString().substring(length + 1, (length + 1)+this.Str.length() - (length + 1));
                    
                    if (Strings.isNullOrEmpty(str)) {
                        throw new Error(String.format("Cannot parse {0} : right Expression of comparaison operator index {1} is empty", this.Str, length));
                    }
                    criterionByName.Literal = str;
                    criterionByName.Operator = operator;
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
