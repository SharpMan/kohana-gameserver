package koh.game.entities.item.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.PlayerEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddExperience extends ItemAction {

    private int exp;

    public AddExperience(String[] args, String criteria) {
        super(args, criteria);
        this.exp = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p))
            return false;
        p.addExperience(exp);
        return true;
    }
}
