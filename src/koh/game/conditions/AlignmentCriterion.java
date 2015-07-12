package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class AlignmentCriterion extends Criterion {

    public byte AlignmentSide;

    @Override
    public void Build() {
        int result;
        try {
            result = Integer.parseInt(Literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build AdminRightsCriterion, {0} is not a valid role", this.Literal));
        }
        this.AlignmentSide = (byte) result;
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare((Comparable<Byte>) character.AlignmentSide.value, this.AlignmentSide);
    }

    @Override
    public String toString() {
        return this.FormatToString("Ps");
    }

}
