package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class AlignmentCriterion extends Criterion {

    public byte alignmentSide;

    @Override
    public void Build() {
        int result;
        try {
            result = Integer.parseInt(literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build AdminRightsCriterion, {0} is not a valid role", this.literal));
        }
        this.alignmentSide = (byte) result;
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Byte>) character.getAlignmentSide().value, this.alignmentSide);
    }

    @Override
    public String toString() {
        return this.FormatToString("Ps");
    }

}
