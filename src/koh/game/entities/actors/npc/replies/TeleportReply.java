package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcReply;

/**
 *
 * @author Neo-Craft
 */
public class TeleportReply extends NpcReply {
    
    @Override
    public boolean Execute(Player p) {
        if (!super.Execute(p)) {
            return false;
        }
        p.teleport(Integer.parseInt(this.Parameters[0]), Integer.parseInt(this.Parameters[1]));
        return true;
    }
    
}
