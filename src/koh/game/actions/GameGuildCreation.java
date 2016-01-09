package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.protocol.messages.game.guild.GuildCreationStartedMessage;

/**
 *
 * @author Neo-Craft
 */
public class GameGuildCreation extends GameAction {

    public GameGuildCreation(IGameActor actor) {
        super(GameActionTypeEnum.CREATE_GUILD, actor);

    }

    @Override
    public void execute() {
        actor.send(new GuildCreationStartedMessage());
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
