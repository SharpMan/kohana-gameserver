package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class AlignementLevelCriterion extends Criterion {

    public static String Identifier = "Pa";

    @Override
    public String toString() {
        return this.FormatToString("Pa");
    }
    
    public int Level;

    @Override
    public void Build() {
        int result;
        try {
            result = Integer.parseInt(Literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build AlignementLevelCriterion, {0} is not a valid alignement level", this.Literal));
        }
        this.Level = result;
    }

    @Override
    public boolean eval(Player character) {
        return true;
    }

    

}
