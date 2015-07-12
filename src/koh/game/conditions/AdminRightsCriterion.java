package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class AdminRightsCriterion extends Criterion {

    public final static String Identifier = "PX";

    public byte Role;

    public AdminRightsCriterion() {
    }

    @Override
    public void Build() {
        if (this.Literal.equalsIgnoreCase("G")) {
            this.Role = 0; //Player
        }
        int result;
        try {
            result = Integer.parseInt(Literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build AdminRightsCriterion, {0} is not a valid role", this.Literal));
        }
        this.Role = (byte) result;
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Byte>) character.Account.Right, this.Role);
    }
    
    @Override
    public String toString(){
         return this.FormatToString("PX");
    }

}
