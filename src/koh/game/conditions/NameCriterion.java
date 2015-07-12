package koh.game.conditions;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class NameCriterion extends Criterion {

    public static String Identifier = "PN";
    public String Name;

    @Override
    public String toString() {
        return this.FormatToString("PN");
    }

    @Override
    public void Build() {
        this.Name = this.Literal;
    }

    @Override
    public boolean Eval(Player character) {
        return this.Compare(character.NickName, this.Name);
    }
}
