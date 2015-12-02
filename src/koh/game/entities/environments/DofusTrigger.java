package koh.game.entities.environments;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class DofusTrigger {

    public int type;
    public int newMap;
    public int newCell;
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

    
    public void apply(Player Actor) {
        switch (this.type) {
            case 0:
                /*if (!this.criteria.isEmpty() && !ConditionParserOld.validConditions(actor, criteria)) { Dont need to check now
                    PlayerController.sendServerMessage(actor.client, "Vous n'avez pas les condition suffisantes...");
                    return;
                }*/
                Actor.teleport(newMap, newCell);
                break;
        }
    }

}
