package koh.game.entities.command;

import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/11/16.
 */
public class ShowDoorsCommand implements PlayerCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        StringBuilder sb = new StringBuilder("Map ");
        sb.append(client.getCharacter().getCurrentMap().getId()).append(" DoorList\n");
        client.getCharacter().getCurrentMap().getDoors().values().forEach(d -> {
            sb.append("Door ").append(d.getElementID());
            sb.append(" Param ").append(d.getParameters()).append("\n");
        });
        client.send(new ConsoleMessage((byte)1, sb.toString()));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 1;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}
