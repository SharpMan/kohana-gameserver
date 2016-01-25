package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.MovementPath;
import koh.game.entities.maps.pathfinding.PathElement;
import koh.game.entities.maps.pathfinding.Pathfinding;
import koh.game.network.WorldClient;
import koh.protocol.messages.game.context.ShowCellMessage;

/**
 * Created by Melancholia on 12/10/15.
 * This is just a test file
 */
public class TeleportCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "Teleporte a la map arg1 et cellule arg2";
    }

    @Override
    public void apply(WorldClient client, String args[]) {
        int mapid = Integer.parseInt(args[0]);
        int cellid = Integer.parseInt(args[1]);
        client.getCharacter().teleport(mapid, cellid);
        /*try {
            final MovementPath p = Pathfinding.findPath(client.getCharacter().getFight(), client.getCharacter().getFighter().getMapPoint(), MapPoint.fromCellId(cellid), false,false);
            System.out.println(client.getCharacter().getFighter().getMapPoint().toString());
            System.out.println(MapPoint.fromCellId(cellid));

            PlayerController.sendServerMessage(client,p.toString());
            for (PathElement pp : p.get_path()) {
                client.send(new ShowCellMessage(0, pp.get_cellId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public boolean can(WorldClient client) {

        if(client.isGameAction(GameActionTypeEnum.FIGHT)){
            PlayerController.sendServerMessage(client, "Action impossible : Vous etes en combat");
            return false;
        }
        if(!client.canGameAction(GameActionTypeEnum.MAP_MOVEMENT)){
            PlayerController.sendServerMessage(client, "Action impossible : Vous etes occupe"); //pas d'accent en
            return false;
        }
        return true;
    }

    @Override
    public int roleRestrained() {
        return 1;
    }

    @Override
    public int argsNeeded() {
        return 2;
    }
}
