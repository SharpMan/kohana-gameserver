package koh.game.conditions;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public class ServerCriterion extends Criterion {

    public static String Identifier = "SI";
    public int Server;

    @Override
    public String toString() {
        return this.FormatToString("SI");
    }

    @Override
    public void Build() {
        Server = Integer.parseInt(literal);
    }

    @Override
    public boolean eval(Player character) {
        return DAO.getSettings().getIntElement("World.id") == this.Server;
    }
}
