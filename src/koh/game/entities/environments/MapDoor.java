package koh.game.entities.environments;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MapDoor {

    public int ElementID;
    public int Type, Map;
    public String Parameters;
    public String Criteria;

    private ConditionExpression m_criteriaExpression;

    public ConditionExpression CriteriaExpression() {
        if (m_criteriaExpression == null) {
            if (Strings.isNullOrEmpty(Criteria) || this.Criteria.equalsIgnoreCase("null")) {
                return null;
            } else {
                this.m_criteriaExpression = ConditionExpression.Parse(this.Criteria);
            }
        }
        return m_criteriaExpression;
    }

    public boolean AreConditionFilled(Player character) {
        try {
            if (this.CriteriaExpression() == null) {
                return true;
            } else {
                return this.CriteriaExpression().Eval(character);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
