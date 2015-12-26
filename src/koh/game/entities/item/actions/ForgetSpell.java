package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/13/15.
 */
public class ForgetSpell  extends ItemAction {

    public ForgetSpell(String[] args, String criteria, int template) {
        super(args, criteria, template);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        //TODO: Open ForgetSpell GUi + write GameAction
        return true;
    }
}
