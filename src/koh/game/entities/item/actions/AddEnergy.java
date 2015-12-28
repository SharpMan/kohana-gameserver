package koh.game.entities.item.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.PlayerEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;

/**
 * Created by Melancholia on 12/13/15.
 */
public class AddEnergy extends ItemAction {

    private int energy;

    public AddEnergy(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.energy = Integer.parseInt(args[0]);
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell))
            return false;
        int val = energy;
        p.setEnergy(p.getEnergy() + energy);
        if(p.getEnergy() +val > PlayerEnum.MAX_ENERGY){
            val = PlayerEnum.MAX_ENERGY - p.getEnergy();
        }
        p.setEnergy(val);
        p.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE,7,String.valueOf(val)));
        p.refreshStats();
        return true;
    }
}
