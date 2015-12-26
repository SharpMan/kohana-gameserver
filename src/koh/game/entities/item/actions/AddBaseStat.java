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

    public AddBaseStat(String[] args, String criteria, int template) {
        super(args, criteria,  template);
        this.statId = Integer.parseInt(args[0]);
        this.number = Short.parseShort(args[1]);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        //TODO : LOG it in db column
        p.getStats().addBase(StatsEnum.valueOf(statId),number);
        p.refreshStats();
        return true;
    }
}
