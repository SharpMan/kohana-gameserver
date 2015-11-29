package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.NpcDialog;
import koh.game.dao.mysql.NpcDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.npc.NpcReply;

/**
 *
 * @author Neo-Craft
 */
public class TalkReply extends NpcReply {

    @Override
    public boolean Execute(Player p) {
        if (!super.Execute(p)) {
            return false;
        }
        ((NpcDialog) p.Client.GetGameAction(GameActionTypeEnum.NPC_DAILOG)).ChangeMessage(NpcDAOImpl.messages.get(Integer.parseInt(this.Parameters[0])));
        return true;
    }

}
