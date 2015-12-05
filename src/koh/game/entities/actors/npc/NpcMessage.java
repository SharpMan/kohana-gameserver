package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class NpcMessage {

    public int id, messageId;
    public String[] parameters;
    public String criteria;
    public int falseQuestion;
    public int[] replies;

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

    public String[] getParameters(Player req) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].contains("BK")) {
                parameters[i] = String.valueOf(req.account.accountData.itemscache.size()); //TODO : kamas Bank
            }
        }
        return parameters;
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

    public NpcMessage getMessage(Player req) {
        if (areConditionFilled(req)) {
            return this;
        } else {
            return DAO.getNpcs().findMessage(this.falseQuestion).getMessage(req);
        }
    }

}
