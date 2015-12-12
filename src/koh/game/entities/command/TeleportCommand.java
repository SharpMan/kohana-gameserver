package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;

/**
 * Created by Melancholia on 12/10/15.
 * This is just a test file
 */
public class TeleportCommand implements PlayerCommand {

    @Override
    public String getDescription() {
        return "Teleporte a la map {1} et cellule {2}";
    }

    @Override
    public void apply(WorldClient client, String args) {
        int mapid = Integer.parseInt(args.split(" ")[0]);
        int cellid = args.split(" ").length < 1 ? -1 : Integer.parseInt(args.split(" ")[1]);
        client.character.teleport(mapid, cellid);
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
}
