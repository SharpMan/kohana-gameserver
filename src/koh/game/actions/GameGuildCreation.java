package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.protocol.messages.game.guild.GuildCreationStartedMessage;

/**
 *
 * @author Neo-Craft
 */
public class GameGuildCreation extends GameAction {

    public GameGuildCreation(IGameActor Actor) {
        super(GameActionTypeEnum.CREATE_GUILD, Actor);

    }

    @Override
    public void Execute() {
        Actor.Send(new GuildCreationStartedMessage());
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
