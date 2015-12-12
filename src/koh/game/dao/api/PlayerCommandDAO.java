package koh.game.dao.api;

import koh.game.entities.command.PlayerCommand;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 12/10/15.
 */
public abstract class PlayerCommandDAO implements Service {


    public abstract PlayerCommand findChatCommand(String message);

    public abstract PlayerCommand findConsoleCommand(String message);
}
