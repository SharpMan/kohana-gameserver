package koh.game.entities.command;

import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;
import koh.protocol.client.enums.AlignmentSideEnum;

import java.sql.SQLException;

/**
 * Created by Melancholia on 1/16/17.
 */
public class SerianneCommand implements PlayerCommand {
    @Override
    public String getDescription() {
        return "Change l'alignement en serianne";
    }

    @Override
    public void apply(WorldClient client, String[] args) throws SQLException {
        if(client.getCharacter().getAlignmentSide() == AlignmentSideEnum.ALIGNMENT_MERCENARY){
            PlayerController.sendServerErrorMessage(client,"Vous avez deja l'alignement");
        }else if(client.getCharacter().getHonor() < 20000){
            PlayerController.sendServerErrorMessage(client,"Vous devriez avoir au moins 20000 points d'honneur pour rejointre notre clan !");
        }else{
            client.getCharacter().changeAlignementSide(AlignmentSideEnum.ALIGNMENT_MERCENARY);
        }
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
        return 0;
    }
}
