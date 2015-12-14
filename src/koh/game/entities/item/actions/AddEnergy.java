package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.PlayerEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddEnergy extends ItemAction {

    private int energy;

    public AddEnergy(String[] args, String criteria) {
        super(args, criteria);
        this.energy = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p))
            return false;
        p.setEnergy(p.getEnergy() + energy);
        if(p.getEnergy() > PlayerEnum.MaxEnergy){
            p.setEnergy(PlayerEnum.MaxEnergy);
        }
        p.refreshStats();
        return true;
    }
}
