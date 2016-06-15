package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/12/15.
 */
public class Kamas extends ItemAction {

    public int count;

    public Kamas(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.count = Integer.parseInt(args[0]);
    }


    @Override
    public boolean execute(Player possessor,Player p, int cell) {
        if(!super.execute(possessor,p, cell))
            return false;
        int curKamas = p.getKamas();
        int newKamas = curKamas + count;
        if(newKamas <0) newKamas = 0;
        p.getInventoryCache().addKamas(newKamas);
        return true;
    }
}
