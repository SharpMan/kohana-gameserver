package koh.game.conditions;

import com.google.common.primitives.Bytes;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class EmoteCriterion extends Criterion {

    public static String Identifier = "PE";
    public byte Emote;

    @Override
    public String toString() {
        return this.FormatToString("PE");
    }

    @Override
    public void Build() {
        this.Emote = Byte.parseByte(Literal);;
    }

    @Override
    public boolean eval(Player character) {
        //FIXME : See if another operator exist for this
        switch (this.Operator) {
            case INEQUALS:
                return !Bytes.contains(character.emotes, Emote);
            case EQUALS:
                return Bytes.contains(character.emotes, Emote);
        }
        return true;
    }

}
