package koh.game.entities.item.actions;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.messages.server.basic.SystemMessageDisplayMessage;

import java.util.HashMap;

/**
 * Created by Melancholia on 8/7/16.
 */
public class AddBoolean extends ItemAction {

    private int bool;

    public static final short TRUE = 1;
    private int message = -1;

    public AddBoolean(String[] args, String criteria, int template) {
        super(args, criteria, template);
        this.bool = Integer.parseInt(args[0]);
        if(args.length > 1){
            message = Integer.parseInt(args[1]);
        }
    }

    @Override
    public boolean execute(Player possessor, Player p, int cell) {
        if (!super.execute(possessor, p, cell) || !p.getClient().canGameAction(GameActionTypeEnum.CHANGE_MAP))
            return false;
        if (p.getBooleans() == null) {
            p.setBooleans(new HashMap<>(6));
        }
        p.getBooleans().replace(bool, TRUE);
        p.getBooleans().putIfAbsent(bool, TRUE);
        if(message != -1){
            p.send(new SystemMessageDisplayMessage(false,message,null));
        }
        return true;
    }
}

