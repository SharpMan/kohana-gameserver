package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddSpellPoint extends ItemAction {

    private int spellPoints;

    public AddSpellPoint(String[] args, String criteria) {
        super(args, criteria);
        this.spellPoints = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || !p.client.canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        p.setSpellPoints(p.getSpellPoints() + spellPoints);
        p.refreshStats();
        return true;
    }
}
