package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.environments.MapDoor;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/11/16.
 */
public class AddDoorCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        final int doorId = Integer.parseInt(args[0]);
        final int map = Integer.parseInt(args[1]);
        final short cell = Short.parseShort(args[2]);
        MapDoor door = client.getCharacter().getCurrentMap().getDoor(doorId);
        if(door == null){
            door = new MapDoor(doorId, 0 , client.getCharacter().getCurrentMap().getId(), map+","+cell, null);
            client.getCharacter().getCurrentMap().addDoor(door);
            DAO.getMaps().insertDoor(door);
            client.send(new ConsoleMessage((byte)1, doorId+" inserted"));

        }else{
            door.setParameters(map+","+cell);
            DAO.getMaps().updateDoor(door);
            client.send(new ConsoleMessage((byte)1, doorId+" updated"));
        }

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
        return 3;
    }
}
