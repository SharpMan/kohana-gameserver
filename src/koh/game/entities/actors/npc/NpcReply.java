package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;
import lombok.Getter;

/**
 *
 * @author Neo-Craft
 */
public class NpcReply {

    private String type;
    @Getter
    private int replyID;
    private String criteria;
    @Getter
    private String[] parameters;

    private ConditionExpression m_criteriaExpression;

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

    public boolean execute(Player p) {
        return areConditionFilled(p);
    }

}
