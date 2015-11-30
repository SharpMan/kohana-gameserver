package koh.game.entities.environments;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class DofusTrigger {

    public int Type;
    public int NewMap;
    public int NewCell;
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

    
    public void Apply(Player Actor) {
        switch (this.Type) {
            case 0:
                /*if (!this.criteria.isEmpty() && !ConditionParserOld.validConditions(actor, criteria)) { Dont need to check now
                    PlayerController.sendServerMessage(actor.client, "Vous n'avez pas les condition suffisantes...");
                    return;
                }*/
                Actor.teleport(NewMap, NewCell);
                break;
        }
    }

}
