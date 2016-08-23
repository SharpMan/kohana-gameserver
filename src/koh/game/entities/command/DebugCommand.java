package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.sql.SQLException;

/**
 * Created by Melancholia on 8/20/16.
 */
public class DebugCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "Debug player-name arg1";
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final Player target = DAO.getPlayers().getCharacter(args[0]);
        if(target == null){
            client.send(new ConsoleMessage((byte)2,"Missing target"));
        }else{
            target.setFight(null);
            target.setFighter(null);
            if(target.getFightsRegistred() != null){
                target.getFightsRegistred().clear();
            }
            target.fightTeleportation(client.getCharacter().getMapid(),client.getCharacter().getCell().getId());
            client.send(new ConsoleMessage((byte)1,"Target debugged"));
        }
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
        return 1;
    }
}
