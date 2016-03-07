package koh.game.network.handlers.character;

import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.command.PlayerCommand;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.authorized.AdminCommandMessage;
import koh.protocol.messages.authorized.ConsoleMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.function.Consumer;

/**
 * Created by Melancholia on 1/9/16.
 */
public class AuthorizedHandler {

    private static final Logger logger = LogManager.getLogger("Command");

    @HandlerAttribute(ID = AdminCommandMessage.MESSAGE_ID)
    public static void handlerAdminCommandMessage(WorldClient client , AdminCommandMessage message){
        String [] args = message.content.split(" ");
        PlayerCommand consoleCommand = DAO.getCommands().findConsoleCommand(args[0]);
        log((logger) -> logger.info(message.content), client);

        if(consoleCommand != null){
            consoleCommand.call(client, message.content.substring(args.length > 1 ? (args[0].length() +1) : args[0].length()));
        }
        else{
            client.send(new ConsoleMessage((byte)0, "Commande introuvable, veuillez joindre la commande help pour explorer les commandes !"));
        }
    }

    private static void log(Consumer<Logger> writer, WorldClient client) {
        ThreadContext.put("clientAddress", client.getIP());
        ThreadContext.put("playerName", client.getCharacter().getNickName());
        ThreadContext.put("playerNickname", client.getAccount().nickName);

        try {
            writer.accept(logger);

        } finally {
            ThreadContext.remove("clientAddress");
            ThreadContext.remove("playerName");
            ThreadContext.remove("playerNickName");

        }
    }

}
