package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class NpcReply {

    public String type;
    public int replyID;
    public String criteria;
    public String[] parameters;

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

    public boolean execute(Player p) {
        return areConditionFilled(p);
    }

}
