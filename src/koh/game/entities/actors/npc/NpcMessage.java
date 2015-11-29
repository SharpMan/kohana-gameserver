package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.dao.mysql.NpcDAO;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class NpcMessage {

    public int Id, MessageId;
    public String[] Parameters;
    public String Criteria;
    public int FalseQuestion;
    public int[] Replies;

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

    public String[] GetParameters(Player req) {
        for (int i = 0; i < Parameters.length; i++) {
            if (Parameters[i].contains("BK")) {
                Parameters[i] = String.valueOf(req.Account.Data.ItemsCache.size()); //TODO : Kamas Bank
            }
        }
        return Parameters;
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

    public NpcMessage GetMessage(Player req) {
        if (AreConditionFilled(req)) {
            return this;
        } else {
            return NpcDAO.Messages.get(this.FalseQuestion).GetMessage(req);
        }
    }

}
