package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class HasItemCriterion extends Criterion {

    public static String Identifier = "PO";
    public int Item;

    @Override
    public String toString() {
        return this.FormatToString("PO");
    }

    @Override
    public void Build() {
        try {
            this.Item = Integer.parseInt(literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build HasItemCriterion, {0} is not a valid item id", this.literal));
        }
    }

    @Override
    public boolean eval(Player character) {
        if (this.operator == ComparaisonOperatorEnum.EQUALS) {
            return character.getInventoryCache().hasItemId(this.Item);
        }
        else if (this.operator == ComparaisonOperatorEnum.INEQUALS) {
            return !character.getInventoryCache().hasItemId(this.Item);
        } else {
            return true;
        }
    }

}
