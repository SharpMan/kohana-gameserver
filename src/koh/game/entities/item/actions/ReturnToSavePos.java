package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/12/15.
 */
public class ReturnToSavePos  extends ItemAction {

    public ReturnToSavePos(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        p.teleport(p.getSavedMap(), p.getSavedCell());
        return true;
    }
}
