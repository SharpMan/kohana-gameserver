package koh.game.entities.command;

import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 12/16/16.
 */
public class SetEventCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "set the current map to .event";
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        EventCommand.MAP = client.getCharacter().getMapid();
        EventCommand.CELL = client.getCharacter().getCell().getId();
        client.send(new ConsoleMessage((byte)2, "map raffrachis"));
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
        return 0;
    }
}
