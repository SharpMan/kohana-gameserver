package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class MapCharactersCriterion extends Criterion {

    public static String Identifier = "MK";

    public int MapId;
    public int CharactersCount;

    @Override
    public String toString() {
        return this.FormatToString("MK");
    }

    @Override
    public void Build() {
        if (this.Literal.contains(",")) {
            this.CharactersCount = Integer.parseInt(Literal.split(",")[0]);
            this.MapId = Integer.parseInt(Literal.split(",")[1]);
        } else {
            this.CharactersCount = Integer.parseInt(Literal);
            this.MapId = -1;
        }
    }

    @Override
    public boolean Eval(Player character) {
        int count = character.currentMap.playersCount();
        if (this.MapId != -1) {
            return this.Compare((Comparable<Integer>) character.currentMap.getId(), this.MapId) && this.Compare((Comparable<Integer>) count, this.CharactersCount);
        } else {
            return this.Compare((Comparable<Integer>) count, this.CharactersCount);
        }
    }

}
