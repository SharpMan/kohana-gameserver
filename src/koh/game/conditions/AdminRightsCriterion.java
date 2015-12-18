package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class AdminRightsCriterion extends Criterion {

    public final static String Identifier = "PX";

    public byte role;

    public AdminRightsCriterion() {
    }

    @Override
    public void Build() {
        if (this.literal.equalsIgnoreCase("G")) {
            this.role = 0; //player
        }
        int result;
        try {
            result = Integer.parseInt(literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build AdminRightsCriterion, {0} is not a valid role", this.literal));
        }
        this.role = (byte) result;
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Byte>) character.getAccount().right, this.role);
    }
    
    @Override
    public String toString(){
         return this.FormatToString("PX");
    }

}
