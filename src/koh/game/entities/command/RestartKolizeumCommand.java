package koh.game.entities.command;

import koh.game.entities.kolissium.KolizeumExecutor;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 6/16/16.
 */
public class RestartKolizeumCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        WorldServer.getKoli().shutdown();
        WorldServer.setKoli(new KolizeumExecutor());
        client.send(new ConsoleMessage((byte) 0, "Kolizeum Task rescheduled"));
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
        return 0;
    }
}
