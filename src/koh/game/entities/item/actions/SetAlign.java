package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.AlignmentSideEnum;

/**
 * Created by Melancholia on 12/13/15.
 */
public class SetAlign  extends ItemAction {

    private byte newAlign;
    private boolean replace;


    public SetAlign(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.newAlign = Byte.parseByte(args[0]);
        this.replace = Integer.parseInt(args[1]) == 1;
    }

    @Override
    public boolean execute(Player p, int cell) {
        if(!super.execute(p, cell) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        if(p.getAlignmentSide() != AlignmentSideEnum.ALIGNMENT_NEUTRAL && !replace)
            return false;
        p.changeAlignementSide(AlignmentSideEnum.valueOf(newAlign));
        return true;
    }
}
