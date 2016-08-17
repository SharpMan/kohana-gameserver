package koh.game.entities.command;

import koh.game.controllers.PlayerController;
import koh.game.dao.api.AreaDAO;
import koh.game.network.WorldClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * Created by Melancholia on 12/10/15.
 */
public interface PlayerCommand {

    static final Logger logger = LogManager.getLogger(PlayerCommand.class);

    public String getDescription();

    public default void call(WorldClient client, String args){
        if(client.getAccount().accountData.right < this.roleRestrained()){
            PlayerController.sendServerMessage(client, "You do not have the appropriate rank");
        }
        else if(args.split(" ").length < this.argsNeeded()){
            PlayerController.sendServerMessage(client,"Valeurs incorrects, cette commande demande "+this.argsNeeded()+" arguments","01EA85");
        }
        else if(this.can(client)){
            try {
                this.apply(client, args.split(" ",this.argsNeeded()));
            }catch(Exception e){
                logger.error(e);
                logger.warn(e.getMessage());
                PlayerController.sendServerMessage(client, "Please enter correctly the command's args");
                if(this.getDescription() != null)
                    PlayerController.sendServerMessage(client, this.getDescription());
            }
        }
    }

    public void apply(WorldClient client,String[] args) throws SQLException;

    public boolean can(WorldClient client);

    public int roleRestrained();

    public int argsNeeded();


}
