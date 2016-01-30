package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.entities.environments.DofusCell;
import koh.game.fights.utils.*;
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
        final int mapid = Integer.parseInt(args[0]);
        final short cellid = Short.parseShort(args[1]);
        client.getCharacter().teleport(mapid, cellid);
        /*try {
            final Path p = new Pathfinder(client.getCharacter().getCurrentMap(), client.getCharacter().getFight(), true, false).findPath(client.getCharacter().getFight().getMap().getCell(client.getCharacter().getFighter().getCellId()),client.getCharacter().getFight().getMap().getCell(cellid), false,6);


            PlayerController.sendServerMessage(client,p.getCellsPath().length+" ");
            for (DofusCell pp : p.getCellsPath()) {
                client.send(new ShowCellMessage(0, pp.getId()));
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
