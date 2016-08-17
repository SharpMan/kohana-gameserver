package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/12/16.
 */
public class Search2 implements PlayerCommand {

    @Override
    public String getDescription() {
        return "look for monster contains name arg1";
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        StringBuilder sb = new StringBuilder("Mob containing ");
        sb.append(args[0]).append(" in the name\n");
        DAO.getMonsters().getTemplates()
                .values()
                .stream()
                .filter(e -> e.getNameId().toLowerCase().contains(args[0].trim().toLowerCase()))
                .forEach(mb -> {
                    sb.append(mb.getId()).append(" ").append(mb.getNameId()).append("\n");
                    mb.getGrades().forEach(gr -> {
                        sb.append("grade ").append(gr.getGrade()).append(" level ");
                        sb.append(gr.getLevel()).append("\n");
                    });
                });
        client.send(new ConsoleMessage((byte)0,sb.toString()));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 2;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
