package koh.game.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.protocol.messages.game.context.roleplay.party.PartyLeaveMessage;

/**
 *
 * @author Neo-Craft
 */
public class GameParty extends GameAction {

    public Party Party;

    public Player player;

    public GameParty(Player p1, Party p) {
        super(GameActionTypeEnum.GROUP, p1);
        Party = p;
        this.player = p1;
    }

    @Override
    public void EndExecute() throws Exception {
        Party.Leave(this.player,false);
        player.Send(new PartyLeaveMessage(Party.ID));
        super.EndExecute();
    }

    @Override
    public void Abort(Object[] Args) {
        Party.Leave(this.player,false);
        super.Abort(Args);
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        if (ActionType == GameActionTypeEnum.GROUP) {
            return false;
        }
        return true;
    }

}
