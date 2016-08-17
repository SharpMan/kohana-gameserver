package koh.game.entities.environments;

import com.google.common.base.Strings;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;
import lombok.Getter;
import lombok.Setter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Neo-Craft
 */
public class MapDoor {

    @Getter
    private int elementID;
    @Getter
    private int type, map;
    @Getter @Setter
    private String parameters;
    private String criteria;

    public MapDoor(int ele, int type , int map, String param, String citeria){
        this.elementID = ele;
        this.type =type;
        this.map = map;
        this.parameters = param;
        this.criteria = citeria;
    }

    private ConditionExpression m_criteriaExpression;

    public MapDoor(ResultSet result) throws SQLException {
        this.elementID = result.getInt("elem_id");
        this.map = result.getInt("map");
        this.type = result.getInt("type");
        this.parameters = result.getString("parameters");
        this.criteria = result.getString("criteria");
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

}
