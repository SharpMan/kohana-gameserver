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


    public SetAlign(String[] args, String criteria) {
        super(args, criteria);
        this.newAlign = Byte.parseByte(args[0]);
        this.replace = Integer.parseInt(args[1]) == 1;
    }

    @Override
    public boolean execute(Player p) {
        if(!super.execute(p) || p.getClient().isGameAction(GameActionTypeEnum.FIGHT))
            return false;
        if(p.alignmentSide!= AlignmentSideEnum.ALIGNMENT_NEUTRAL && !replace)
            return false;
        p.changeAlignementSide(AlignmentSideEnum.valueOf(newAlign));
        return true;
    }
}
