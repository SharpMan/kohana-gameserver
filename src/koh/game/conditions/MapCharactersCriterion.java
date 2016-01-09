package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MapCharactersCriterion extends Criterion {

    public static final String IDENTIFIER = "MK";

    public int mapId;
    public int charactersCount;

    @Override
    public String toString() {
        return this.FormatToString("MK");
    }

    @Override
    public void Build() {
        if (this.literal.contains(",")) {
            this.charactersCount = Integer.parseInt(literal.split(",")[0]);
            this.mapId = Integer.parseInt(literal.split(",")[1]);
        } else {
            this.charactersCount = Integer.parseInt(literal);
            this.mapId = -1;
        }
    }

    @Override
    public boolean eval(Player character) {
        int count = character.getCurrentMap().playersCount();
        if (this.mapId != -1) {
            return this.Compare((Comparable<Integer>) character.getCurrentMap().getId(), this.mapId) && this.Compare((Comparable<Integer>) count, this.charactersCount);
        } else {
            return this.Compare((Comparable<Integer>) count, this.charactersCount);
        }
    }

}
