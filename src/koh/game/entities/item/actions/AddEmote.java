package koh.game.entities.item.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.item.ItemAction;
import koh.protocol.messages.game.context.roleplay.emote.EmoteListMessage;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by Melancholia on 1/30/17.
 */
public class AddEmote extends ItemAction {

    private byte id;

    public AddEmote(String[] args, String criteria, int template) {
        super(args, criteria, template);
        try {
            this.id = (byte) Integer.parseInt(args[0]);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean execute(Player possessor, Player p, int cell) {
        if(!super.execute(possessor,p, cell) )
            return false;
        p.setEmotes(ArrayUtils.add(p.getEmotes(), id));
        p.send(new EmoteListMessage(p.getEmotes()));
        p.getInventoryCache().safeDelete(template,1);
        //all job is auto learned
        return true;
    }
}
