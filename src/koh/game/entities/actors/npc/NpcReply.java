package koh.game.entities.actors.npc;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class NpcReply {

    public String Type;
    public int ReplyID;
    public String Criteria;
    public String[] Parameters;

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

    public boolean Execute(Player p) {
        return AreConditionFilled(p);
    }

}
