package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.environments.DofusTrigger;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/11/16.
 */
public class AddTriggerCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args)  {
        final int map = Integer.parseInt(args[0]);
        final short cell = Short.parseShort(args[1]);
        client.getCharacter().getCell().myAction = new DofusTrigger(map,cell);
        DAO.getMaps().insert(client.getCharacter().getCurrentMap().getId(), client.getCharacter().getCell().getId(), client.getCharacter().getCell().myAction);

        client.send(new ConsoleMessage((byte)1, "The map trigger had been successfully updated"));
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
        return 2;
    }
}
