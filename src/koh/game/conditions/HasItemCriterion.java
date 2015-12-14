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
            this.Item = Integer.parseInt(Literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build HasItemCriterion, {0} is not a valid item id", this.Literal));
        }
    }

    @Override
    public boolean eval(Player character) {
        if (this.Operator == ComparaisonOperatorEnum.EQUALS) {
            return character.inventoryCache.hasItemId(this.Item);
        }
        else if (this.Operator == ComparaisonOperatorEnum.INEQUALS) {
            return !character.inventoryCache.hasItemId(this.Item);
        } else {
            return true;
        }
    }

}
