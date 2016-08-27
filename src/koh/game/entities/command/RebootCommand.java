package koh.game.entities.command;

import koh.game.Main;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/24/16.
 */
public class RebootCommand implements PlayerCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        Main.scheduleReboot(Integer.parseInt(args[0]));
        client.send(new ConsoleMessage((byte)2, "Reboot collector successfully re-scheduled"));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 3;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
