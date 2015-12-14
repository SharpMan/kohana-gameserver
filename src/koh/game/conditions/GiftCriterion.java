package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class GiftCriterion extends Criterion {

    public static String Identifier = "Pg";

    public Integer Id;
    public Integer Level;

    @Override
    public String toString() {
        return this.FormatToString("Pg");
    }

    @Override
    public void Build() {
        int result1 = -1;
        int result2;
        if (this.Literal.contains(",")) {
            try {
                String[] strArray = this.Literal.split(",");
                result2 = Integer.parseInt(strArray[0]);
                result1 = Integer.parseInt(strArray[1]);
            } catch (Exception e) {
                throw new Error(String.format("Cannot build GiftCriterion, {0} is not a valid gift (format 'id,level')", this.Literal));
            }
        } else {
            try {
                result2 = Integer.parseInt(Literal);
            } catch (Exception ee) {
                throw new Error(String.format("Cannot build GiftCriterion, {0} is not a valid gift (format 'id,level')", this.Literal));
            }
        }
        this.Id = result2;
        this.Level = result1;
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

}
