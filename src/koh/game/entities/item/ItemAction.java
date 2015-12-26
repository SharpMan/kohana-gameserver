package koh.game.entities.item;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import lombok.Builder;
import lombok.Getter;

/**
 * Created by Melancholia on 12/12/15.
 */
public abstract class ItemAction {

    @Getter
    protected String[] args;
    protected String criteria;
    protected int template;

    private ConditionExpression m_criteriaExpression;

    public ItemAction(String[] args, String criteria, int template) {
        this.args = args;
        this.criteria = criteria;
        this.template = template;
    }

    private ConditionExpression getCriteriaExpression() {
        if (m_criteriaExpression == null) {
            if (Strings.isNullOrEmpty(criteria) || this.criteria.equalsIgnoreCase("null")) {
                return null;
            } else {
                this.m_criteriaExpression = ConditionExpression.parse(this.criteria);
            }
        }
        return m_criteriaExpression;
    }

    protected boolean areConditionFilled(Player character) {
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

    public ItemTemplate geTemplate(){
        return DAO.getItemTemplates().getTemplate(this.template);
    }

    public boolean execute(Player p, int cell) {
        if(!areConditionFilled(p)){
            return false;
        }
        return true;
    }

}
