package koh.game.dao.script;

import koh.game.dao.api.PlayerCommandDAO;
import koh.game.entities.command.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by Melancholia on 12/10/15.
 */
public class PlayerCommandDAOImpl extends PlayerCommandDAO {

    private static final Logger logger = LogManager.getLogger(PlayerCommandDAO.class);

    private final HashMap<String, PlayerCommand> chatCommands = new HashMap<>(20), consoleCommands = new HashMap<>(20);

    @Override
    public PlayerCommand findChatCommand(String message) {
        return this.chatCommands.entrySet()
                .stream()
                .filter(key -> message.startsWith(key.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);
    }

    @Override
    public PlayerCommand findConsoleCommand(String message) {
        return this.consoleCommands.entrySet()
                .stream()
                .filter(key -> message.startsWith(key.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);
    }

    private int loadChatCommands() {
        try {
            this.chatCommands.clear();
            Files.walk(Paths.get("data/script/chat"))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".py"))
                    .forEach((Path file) ->
                    {
                        this.chatCommands.put(file.getFileName()
                                        .toString()
                                        .substring(0, file.getFileName().toString().length() - 10)
                                        .toLowerCase()
                                ,PythonUtils.getJythonObject(PlayerCommand.class, file.toString()));

                    });
            this.chatCommands.put("help", new HelpCommand(this.chatCommands));
            this.chatCommands.put("fm", new FmCommand());
            this.chatCommands.put("exo", new ExoCommand());
            this.chatCommands.put("arme", new WeaponExoCommand());
            this.chatCommands.put("event", new EventCommand());
            this.chatCommands.put("serianne", new SerianneCommand());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return chatCommands.size();
    }

    private int loadConsoleCommands() {
        try {
            this.consoleCommands.clear();
            this.consoleCommands.put("teleport", new TeleportCommand());
            this.consoleCommands.put("restartkolizeum", new RestartKolizeumCommand());
            this.consoleCommands.put("restartdb", new DbRestartCommand());
            this.consoleCommands.put("reloadsetting", new ReloadSettingCommand());
            this.consoleCommands.put("item", new ItemCommand());
            this.consoleCommands.put("show", new ShowCommand());
            this.consoleCommands.put("set", new SetCommand());
            this.consoleCommands.put("get", new SearchMonsterCommand());
            this.consoleCommands.put("search", new Search2());
            this.consoleCommands.put("rrr", new SetRightCommand());
            this.consoleCommands.put("route", new RouteCommand());
            this.consoleCommands.put("addtrigger", new AddTriggerCommand());
            this.consoleCommands.put("doors", new ShowDoorsCommand());
            this.consoleCommands.put("+door", new AddDoorCommand());
            this.consoleCommands.put("endfight", new AddFightActionCommand());
            this.consoleCommands.put("spawnfix", new SpawnFixCommand());
            this.consoleCommands.put("spawns", new SpawnsCommand());
            this.consoleCommands.put("removespawn", new RemoveSpawn());
            this.consoleCommands.put("spawnnpc", new SpawnNPC());
            this.consoleCommands.put("debug", new DebugCommand());
            this.consoleCommands.put("rekt", new RebootCommand());
            this.consoleCommands.put("setevent", new SetEventCommand());


            Files.walk(Paths.get("data/script/console"))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".py"))
                    .forEach((Path file) ->
                    {
                        this.consoleCommands.put(file.getFileName()
                                        .toString()
                                        .substring(0, file.getFileName().toString().length() - 10)
                                        .toLowerCase()
                                , PythonUtils.getJythonObject(PlayerCommand.class, file.toString()));

                    });
            this.consoleCommands.put("help", new HelpCommand(this.consoleCommands));

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return consoleCommands.size();
    }


    @Override
    public void start() {
        logger.info("Loaded {} chat commands", this.loadChatCommands());
        logger.info("Loaded {} console commands", this.loadConsoleCommands());
    }

    @Override
    public void stop() {

    }
}
