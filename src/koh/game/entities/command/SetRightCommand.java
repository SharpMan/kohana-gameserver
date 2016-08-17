package koh.game.entities.command;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;

/**
 * Created by Melancholia on 8/8/16.
 */
public class SetRightCommand implements PlayerCommand {


    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final Player target = DAO.getPlayers().getCharacter(args[0].toLowerCase().trim());
        if(target == null)
            return;
        target.getAccount().accountData.right = Byte.parseByte(args[1]);
        target.getAccount().accountData.notifyColumn("right");
        DAO.getAccountDatas().save(target.getAccount().accountData,target.getAccount());
        PlayerController.sendServerErrorMessage(client, "ds");

    }

    @Override
    public boolean can(WorldClient client) {
        return true;
    }

    @Override
    public int roleRestrained() {
        return 7;
    }

    @Override
    public int argsNeeded() {
        return 2;
    }


}
