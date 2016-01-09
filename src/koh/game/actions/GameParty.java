package koh.game.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.actors.character.Party;
import koh.protocol.messages.game.context.roleplay.party.PartyLeaveMessage;

/**
 *
 * @author Neo-Craft
 */
public class GameParty extends GameAction {

    public Party party;

    public Player player;

    public GameParty(Player p1, Party p) {
        super(GameActionTypeEnum.GROUP, p1);
        party = p;
        this.player = p1;
    }

    @Override
    public void endExecute() throws Exception {
        party.leave(this.player,false);
        player.send(new PartyLeaveMessage(party.id));
        super.endExecute();
    }

    @Override
    public void abort(Object[] Args) {
        party.leave(this.player,false);
        super.abort(Args);
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        if (ActionType == GameActionTypeEnum.GROUP) {
            return false;
        }
        return true;
    }

}
