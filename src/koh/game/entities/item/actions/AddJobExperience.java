package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddJobExperience extends ItemAction {

    private byte jobID;
    private int xpValue;

    public AddJobExperience(String[] args, String criteria, int template) {
        super(args, criteria, template);
        jobID = Byte.parseByte(args[0]);
        xpValue = Integer.parseInt(args[1]);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell))
            return false;
        p.getMyJobs().addExperience(p,jobID,xpValue);
        return true;
    }
}
