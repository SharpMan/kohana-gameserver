package koh.game.entities.actors.pnj.replies;

import koh.game.controllers.PlayerController;
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
            if(this.criteria.startsWith("PL")  && criteria.length() < 7){
                PlayerController.sendServerErrorMessage(p.getClient(), "Vous devez être au moins niveau "+this.criteria.substring(3)+" pour accèder à cette zone.");
            }
            return false;
        }
        p.teleport(Integer.parseInt(this.getParameters()[0]), Integer.parseInt(this.getParameters()[1]));
        return true;
    }
    
}
