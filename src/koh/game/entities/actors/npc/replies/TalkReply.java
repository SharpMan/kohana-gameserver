package koh.game.entities.actors.npc.replies;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.NpcDialog;
import koh.game.dao.DAO;
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
        ((NpcDialog) p.getClient().getGameAction(GameActionTypeEnum.NPC_DAILOG)).changeMessage(DAO.getNpcs().findMessage(Integer.parseInt(this.getParameters()[0])));
        return true;
    }

}
