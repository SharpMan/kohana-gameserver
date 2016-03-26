package koh.game.entities.command;

import koh.game.controllers.PlayerController;
import koh.game.network.WorldClient;
import koh.protocol.messages.authorized.ConsoleMessage;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 1/9/16.
 */
public class HelpCommand implements PlayerCommand {

    private final Map<String, PlayerCommand> commands;

    public HelpCommand(final Map<String, PlayerCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void apply(WorldClient client, String[] args) {
        final StringBuilder sb = new StringBuilder("Commandes disponnibles : \n");
        commands.entrySet().stream()
                .filter(co -> co.getValue().getDescription() != null && client.getAccount().right >= co.getValue().roleRestrained())
                .forEach(co ->
                    sb.append(".").append(co.getKey()).append(" ").append(co.getValue().getDescription()).append("\n")
                );
        if (client.getCharacter().getAccount().right > 0)
            client.send(new ConsoleMessage((byte) 0, sb.toString()));
        else
            PlayerController.sendServerMessage(client, sb.toString());
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
