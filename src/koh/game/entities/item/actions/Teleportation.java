package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.conditions.ConditionExpression;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;

/**
 * Created by Melancholia on 12/12/15.
 */
public class Teleportation extends ItemAction {

    private int map;
    private short cell;

    public Teleportation(String[] args, String criteria) {
        super(args, criteria);
        this.map = Integer.parseInt(args[0]);
        this.cell = Short.parseShort(args[1]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        p.teleport(map, cell);
        return true;
    }
}
