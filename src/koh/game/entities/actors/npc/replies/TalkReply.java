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
    public boolean execute(Player p) {
        if (!super.execute(p)) {
            return false;
        }
        ((NpcDialog) p.client.getGameAction(GameActionTypeEnum.NPC_DAILOG)).changeMessage(NpcDAOImpl.messages.get(Integer.parseInt(this.parameters[0])));
        return true;
    }

}
