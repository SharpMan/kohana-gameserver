package koh.game.conditions;

import com.google.common.primitives.Bytes;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class EmoteCriterion extends Criterion {

    public static final String IDENTIFIER = "PE";
    public byte emote;

    @Override
    public String toString() {
        return this.FormatToString("PE");
    }

    @Override
    public void Build() {
        this.emote = (byte) Integer.parseInt(literal);;
    }

    @Override
    public boolean eval(Player character) {
        switch (this.operator) {
            case INEQUALS:
                return !Bytes.contains(character.getEmotes(), emote);
            case EQUALS:
                return Bytes.contains(character.getEmotes(), emote);
        }
        return true;
    }

}
