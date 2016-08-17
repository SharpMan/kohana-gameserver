package koh.game.entities.command;

import koh.game.dao.DAO;
import koh.game.entities.environments.NeighBourStruct;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

/**
 * Created by Melancholia on 8/10/16.
 */
public class RouteCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final int map = Integer.parseInt(args[1]);
        final short cell = Short.parseShort(args[2]);
        final NeighBourStruct neigh = new NeighBourStruct(map,cell);
        switch (args[0].toLowerCase()){
            case "haut":
                client.getCharacter().getCurrentMap().getNewNeighbour()[0] = neigh;
                break;
            case "bas":
                client.getCharacter().getCurrentMap().getNewNeighbour()[1] = neigh;
                break;
            case "gauche":
                client.getCharacter().getCurrentMap().getNewNeighbour()[2] = neigh;
                break;
            case "droite":
                client.getCharacter().getCurrentMap().getNewNeighbour()[3] = neigh;
                break;
            default:
                client.send(new ConsoleMessage((byte)0, "droite/gache/bas/haut"));
                return;
        }
        DAO.getMaps().updateNeighboor(client.getCharacter().getCurrentMap());
        client.send(new ConsoleMessage((byte)1, "The map had been successfully updated"));
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
