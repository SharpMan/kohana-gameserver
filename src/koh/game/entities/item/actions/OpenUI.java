package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.client.enums.AlignmentSideEnum;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.game.ui.ClientUIOpenedByObjectMessage;

/**
 * Created by Melancholia on 7/3/16.
 */
public class OpenUI extends ItemAction {

    private final int uid;
    private final byte type;

    public OpenUI(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.uid = Integer.parseInt(args[0]);
        this.type = Byte.parseByte(args[1]);
    }

    @Override
    public boolean execute(Player possessor, Player p, int cell) {
        if(!super.execute(possessor,p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.EXCHANGE))
            return false;
        if(possessor != p){
            return false;
        }
        p.send(new PopupWarningMessage((byte) 10, "Symbiote", "1- Placez la mimibiote dans le premier slot"
                + "\n2- Placez l'item ciblé dans le deuxiéme slot\n"
                + "\n3- Placez l'item que vous allez utiliser son apparrence(il doit avoir un niveau inférieur à l'item précedent)"
                + "\n\n4- Deconnectez/Reconnectez vous après avoir remplis les 3 slots !"));
        //final int uid = p.getInventoryCache().findTemplate(14485).getID();
        p.send(new ClientUIOpenedByObjectMessage(p.getInventoryCache().findTemplate(14485).getID(),type));
        return true;
    }



}
