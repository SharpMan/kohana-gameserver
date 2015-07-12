package koh.game.actions.interactive;

import koh.game.entities.actors.Player;

/**
 *
 * @author Neo-Craft
 */
public interface InteractiveAction {
    

    public boolean isEnabled(Player Actor);

    public void Execute(Player Actor, int Element);

    public int GetDuration();
    
    public void Leave(Player Actor, int Element);

    public void Abort(Player player, int Element);

}
