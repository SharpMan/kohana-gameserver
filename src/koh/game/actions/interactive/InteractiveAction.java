package koh.game.actions.interactive;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public interface InteractiveAction {
    

    public boolean isEnabled(Player actor);

    public void execute(Player actor, int element);

    public int getDuration();
    
    public void leave(Player player, int element);

    public void abort(Player player, int element);

}
