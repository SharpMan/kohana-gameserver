package koh.game.entities.command;

import koh.d2o.entities.Monster;
import koh.game.dao.DAO;
import koh.game.entities.actors.MonsterGroup;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/12/16.
 */
public class RemoveSpawn implements PlayerCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        final int id = Integer.parseInt(args[0]);
        if(client.getCharacter().getCurrentMap().getActor(id) == null){
            client.send(new ConsoleMessage((byte)1,"Monster unfound"));
            return;
        }
        final MonsterGroup group = (MonsterGroup) client.getCharacter().getCurrentMap().getActor(id);
        if(group.isFix()){
            DAO.getMapMonsters().remove(client.getCharacter().getCurrentMap().getId(), group.getCell().getId());
        }
        client.getCharacter().getCurrentMap().destroyActor(group);

    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 0;
    }

    @Override
    public int argsNeeded() {
        return 1;
    }
}
