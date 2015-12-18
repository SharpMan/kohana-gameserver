package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class BreedCriterion extends Criterion {

    public Byte Breed;

    @Override
    public void Build() {
        try {
            this.Breed = Byte.parseByte(literal);
        } catch (Exception e) {
            throw new Error(String.format("Cannot build  BreedCriterion, {0} is not a valid alignement level", this.literal));
        }
    }

    @Override
    public boolean eval(Player character) {
        return this.Compare((Comparable<Byte>) this.Breed, character.getBreed());
    }

    @Override
    public String toString() {
        return this.FormatToString("PG");
    }

    public static String Identifier = "PG";

}
