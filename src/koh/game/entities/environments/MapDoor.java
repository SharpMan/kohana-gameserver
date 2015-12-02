package koh.game.entities.environments;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MapDoor {

    public int elementID;
    public int type, map;
    public String parameters;
    public String criteria;

    private ConditionExpression m_criteriaExpression;

    public ConditionExpression getCriteriaExpression() {
        if (m_criteriaExpression == null) {
            if (Strings.isNullOrEmpty(criteria) || this.criteria.equalsIgnoreCase("null")) {
                return null;
            } else {
                this.m_criteriaExpression = ConditionExpression.Parse(this.criteria);
            }
        }
        return m_criteriaExpression;
    }

    public boolean areConditionFilled(Player character) {
        try {
            if (this.getCriteriaExpression() == null) {
                return true;
            } else {
                return this.getCriteriaExpression().Eval(character);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
