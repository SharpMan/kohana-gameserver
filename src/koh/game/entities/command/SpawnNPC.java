package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.actors.Npc;
import koh.game.network.WorldClient;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/19/16.
 */
public class SpawnNPC  implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        final int id = Integer.parseInt(args[0]);
        final Npc pnj = new Npc(client.getCharacter().getCurrentMap().getId(),id,client.getCharacter().getCell().getId(),client.getCharacter().getDirection(), false, 0);
        pnj.setID(client.getCharacter().getCurrentMap().getNextActorId());
        DAO.getNpcs().insert(pnj);
        pnj.setActorCell(client.getCharacter().getCell());
        client.getCharacter().getCurrentMap().spawnActor(pnj);

    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 5;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
