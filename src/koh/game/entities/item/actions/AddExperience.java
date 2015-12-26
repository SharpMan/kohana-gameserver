package koh.game.entities.item.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.PlayerEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddExperience extends ItemAction {

    private  int exp;

    public AddExperience(String[] args, String criteria, int template) {
        super(args, criteria, template);
        try{
            this.exp = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e){
            this.exp = 0;
        }
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell))
            return false;
        p.addExperience(exp);
        return true;
    }
}
