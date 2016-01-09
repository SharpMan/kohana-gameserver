package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcReply;

/**
 *
 * @author Neo-Craft
 */
public class CloseReply extends NpcReply {

    @Override
    public boolean execute(Player p) {
        if (!super.execute(p)) {
            return false;
        }
        try {
            p.getClient().endGameAction(GameActionTypeEnum.NPC_DAILOG);
        } catch (Exception e) {
        }
        return true;
    }

}
