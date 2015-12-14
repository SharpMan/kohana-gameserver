package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/12/15.
 */
public class Kamas extends ItemAction {

    public int count;

    public Kamas(String[] args, String criteria) {
        super(args, criteria);
        this.count = Integer.parseInt(args[0]);
    }


    @Override
    public boolean execute(Player p) {
        if(!super.execute(p))
            return false;
        int curKamas = p.kamas;
        int newKamas = curKamas + count;
        if(newKamas <0) newKamas = 0;
        p.inventoryCache.addKamas(newKamas);
        return true;
    }
}
