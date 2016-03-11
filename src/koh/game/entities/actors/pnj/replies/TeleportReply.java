package koh.game.entities.actors.pnj.replies;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;

/**
 *
 * @author Neo-Craft
 */
public class TeleportReply extends NpcReply {
    
    @Override
    public boolean execute(Player p) {
        if (!super.execute(p)) {
            return false;
        }
        p.teleport(Integer.parseInt(this.getParameters()[0]), Integer.parseInt(this.getParameters()[1]));
        return true;
    }
    
}
