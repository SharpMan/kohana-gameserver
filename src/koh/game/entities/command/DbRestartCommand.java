package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 7/18/16.
 */
public class DbRestartCommand implements PlayerCommand {


    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        DAO.getDbSource().forcedRestart();
        client.send(new ConsoleMessage((byte) 0, "Database restarted"));
    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 6;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}
