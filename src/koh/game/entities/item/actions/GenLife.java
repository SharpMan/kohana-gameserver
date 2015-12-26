package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class GenLife  extends ItemAction {

    private short min;
    private short max;

    public GenLife(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.min = Short.parseShort(args[0]);
        this.max = Short.parseShort(args[1]);
        if(max == 0) max = min;
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        int val = EffectHelper.randomValue(min,max);
        if(p.getLife() + val > p.getMaxLife())val = p.getMaxLife() - p.getLife();
        p.setLife(p.getLife() + val);
        p.refreshStats();
        return true;
    }
}
