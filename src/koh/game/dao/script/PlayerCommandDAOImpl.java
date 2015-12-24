package koh.game.dao.script;

import koh.game.dao.api.AreaDAO;
import koh.game.dao.api.PlayerCommandDAO;
import koh.game.entities.command.PlayerCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * Created by Melancholia on 12/10/15.
 */
public class PlayerCommandDAOImpl extends PlayerCommandDAO {

    private static final Logger logger = LogManager.getLogger(PlayerCommandDAO.class);

    private final HashMap<String, PlayerCommand> chatCommands = new HashMap<>(20), consoleCommands = new HashMap<>(20);

    @Override
    public PlayerCommand findChatCommand(String message){
        return this.chatCommands.entrySet()
                .stream()
                .filter(key -> message.startsWith(key.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElseGet(null);
    }

    @Override
    public PlayerCommand findConsoleCommand(String message){
        return this.consoleCommands.entrySet()
                .stream()
                .filter(key -> message.startsWith(key.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElseGet(null);
    }

    private int loadAll(){
        //TODO : Folder1 = chat, 2 = console
        return 0;
    }



    @Override
    public void start() {
        logger.info("Loaded {} player commands", this.loadAll());

    }

    @Override
    public void stop() {

    }
}
