package koh.game.entities.command;

import koh.game.controllers.PlayerController;
import koh.game.dao.api.AreaDAO;
import koh.game.network.WorldClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Melancholia on 12/10/15.
 */
public interface PlayerCommand {

    public static final Logger logger = LogManager.getLogger(PlayerCommand.class);

    public String getDescription();

    public default void call(WorldClient client, String args){
        if(client.getAccount().right < this.roleRestrained()){
            PlayerController.sendServerMessage(client, "You do not have the appropriate rank");
            return;
        }
        if(this.can(client)){
            try {
                this.apply(client, args);
            }catch(Exception e){
                logger.error(e);
                logger.warn(e.getMessage());
                PlayerController.sendServerMessage(client, "Please enter correctly the command's arg");
                PlayerController.sendServerMessage(client,this.getDescription());
            }
        }
    }

    public void apply(WorldClient client,String args);

    public boolean can(WorldClient client);

    public int roleRestrained();


}
