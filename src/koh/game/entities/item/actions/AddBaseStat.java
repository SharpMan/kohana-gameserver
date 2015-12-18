package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.StatsEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddBaseStat  extends ItemAction {

    private int statId;
    private short number;

    public AddBaseStat(String[] args, String criteria) {
        super(args, criteria);
        this.statId = Integer.parseInt(args[0]);
        this.number = Short.parseShort(args[1]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        //TODO : LOG it in db column
        p.getStats().addBase(StatsEnum.valueOf(statId),number);
        p.refreshStats();
        return true;
    }
}
