package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class Restat  extends ItemAction {

    public Restat(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        p.setLife(Math.max(p.getLife() - p.getVitality(), 0));
        p.setVitality(0);
        p.setStrength(0);
        p.setIntell(0);
        p.setAgility(0);
        p.setChance(0);
        p.getStats().resetBase();
        p.setStatPoints((p.getLevel() - 1) * 5);
        p.refreshStats();
        return true;
    }
}
