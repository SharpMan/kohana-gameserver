package koh.game.entities.actors.pnj.replies;

import koh.game.actions.GameSpellUI;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.pnj.NpcReply;

/**
 * Created by Melancholia on 12/25/16.
 */
public class ForgetSpellReply extends NpcReply {

    @Override
    public boolean execute(Player p) {
        if (!super.execute(p)) {
            return false;}
        p.getClient().addGameAction(new GameSpellUI(p));
        return  true;
    }
}
