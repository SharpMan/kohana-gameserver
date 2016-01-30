package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.utils.Enumerable;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class NpcMessage {

    @Getter
    private int id, messageId;
    @Getter
    private String[] parameters;
    private String criteria;
    private int falseQuestion;
    @Getter
    private int[] replies;

    private ConditionExpression m_criteriaExpression;

    public NpcMessage(ResultSet result) throws SQLException {
        this.id = result.getInt("id");
        this.messageId = result.getInt("message_id");
        this.parameters = result.getString("parameters").split("\\|");
        this.criteria = result.getString("criteria");
        this.falseQuestion = result.getInt("if_false");
        if (result.getString("replies") != null) {
            if (result.getString("replies").isEmpty()) {
                this.replies = new int[0];
            } else {
                this.replies = Enumerable.stringToIntArray(result.getString("replies"));
            }
        }
    }

    public ConditionExpression getCriteriaExpression() {
        if (m_criteriaExpression == null) {
            if (Strings.isNullOrEmpty(criteria) || this.criteria.equalsIgnoreCase("null")) {
                return null;
            } else {
                this.m_criteriaExpression = ConditionExpression.parse(this.criteria);
            }
        }
        return m_criteriaExpression;
    }

    public String[] getParameters(Player req) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].contains("BK")) {
                parameters[i] = String.valueOf(req.getAccount().accountData.itemscache.size()); //TODO : Compute kamas Bank
            }
        }
        return parameters;
    }

    public boolean areConditionFilled(Player character) {
        try {
            if (this.getCriteriaExpression() == null) {
                return true;
            } else {
                return this.getCriteriaExpression().eval(character);
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
