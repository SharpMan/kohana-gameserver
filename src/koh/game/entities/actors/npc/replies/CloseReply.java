package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.NpcDialog;
import koh.game.dao.NpcDAO;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcReply;

/**
 *
 * @author Neo-Craft
 */
public class CloseReply extends NpcReply {

    @Override
    public boolean Execute(Player p) {
        if (!super.Execute(p)) {
            return false;
        }
        try {
            p.Client.EndGameAction(GameActionTypeEnum.NPC_DAILOG);
        } catch (Exception e) {
        }
        return true;
    }

}
