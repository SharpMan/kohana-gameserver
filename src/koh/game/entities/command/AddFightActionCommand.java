package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.environments.MapAction;
import koh.game.entities.environments.MapDoor;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/11/16.
 */
public class AddFightActionCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        final MapAction action = new MapAction(Byte.parseByte(args[0]), args[1]+","+args[2]);
        client.getCharacter().getCurrentMap().addAction(action);
        DAO.getMaps().insertMapAction(client.getCharacter().getCurrentMap().getId(), action);
        client.send(new ConsoleMessage((byte)1, "EndFightAction added on "+client.getCharacter().getMapid()));
        client.getCharacter().getCurrentMap().getMyGameActors()
                .entrySet()
                .stream()
                .filter(e -> e.getValue() instanceof MonsterGroup)
                .map(e -> ((MonsterGroup) e.getValue()))
                .filter(e -> !e.isFix())
                .forEach(e -> {
                    client.getCharacter().getCurrentMap().destroyActor(e);
        });
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
