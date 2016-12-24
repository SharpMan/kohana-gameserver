package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;

import java.sql.SQLException;

/**
 * Created by Melancholia on 12/16/16.
 */
public class EventCommand implements PlayerCommand {

    public static int MAP= 141035520;
    public static  short CELL = 369;


    @Override
    public String getDescription() {
        return null;
    }
    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        client.getCharacter().teleport(MAP,CELL);
    }

    @Override
    public boolean can(WorldClient client) {
        if(!client.canGameAction(GameActionTypeEnum.MAP_MOVEMENT)){
            return false;
        }
        return true;
    }

    @Override
    public int roleRestrained() {
        return 0;
    }

    @Override
    public int argsNeeded() {
        return 0;
    }
}
