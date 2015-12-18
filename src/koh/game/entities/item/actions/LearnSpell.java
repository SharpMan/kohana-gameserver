package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class LearnSpell  extends ItemAction {

    private int id;

    public LearnSpell(String[] args, String criteria) {
        super(args, criteria);
        this.id = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        p.getMySpells().addSpell(id, (byte) 1, p.getMySpells().getFreeSlot(), p.getClient());
        return true;
    }
}
