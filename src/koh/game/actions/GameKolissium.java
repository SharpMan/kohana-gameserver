package koh.game.actions;

import koh.game.entities.actors.Player;
import koh.game.entities.kolissium.ArenaParty;
import koh.game.network.WorldServer;

/**
 * Created by Melancholia on 3/22/16.
 */
public class GameKolissium extends GameAction {

    protected Player player;
    protected ArenaParty party;

    public GameKolissium(Player p1) {
        super(GameActionTypeEnum.KOLI, p1);
        this.player = p1;
    }

    public GameKolissium(Player p1, ArenaParty pp) {
        super(GameActionTypeEnum.KOLI, p1);
        this.player = p1;
        this.party = pp;
    }

    @Override
    public void endExecute() throws Exception {
        if(party != null){
            WorldServer.getKoli().unregisterGroup(party.getChief(), party);
        }
        else
            WorldServer.getKoli().unregisterPlayer(this.player);
        super.endExecute();
    }

    @Override
    public void abort(Object[] Args) {
        if(party != null){
            WorldServer.getKoli().unregisterGroup(party.getChief(), party);
        }
        else
            WorldServer.getKoli().unregisterPlayer(this.player);

        super.abort(Args);
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        if (ActionType == GameActionTypeEnum.KOLI) {
            return false;
        }
        return true;
    }

}
