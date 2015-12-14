package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/12/15.
 */
public class LearnJob extends ItemAction {

    private int id;

    public LearnJob(String[] args, String criteria) {
        super(args, criteria);
        this.id = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) )
            return false;
        //all job is auto learned
        return true;
    }
}
