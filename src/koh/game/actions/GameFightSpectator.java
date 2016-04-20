package koh.game.actions;

import koh.game.fights.Fight;
import koh.game.network.WorldClient;

/**
 * Created by Melancholia on 4/15/16.
 */
public class GameFightSpectator extends GameAction {


    public WorldClient client;

    public Fight fight;

    public GameFightSpectator(WorldClient client, Fight fight){
    super(GameActionTypeEnum.FIGHT, client.getCharacter());

        this.client = client;
        this.fight = fight;
    }

    @Override
    public void abort(Object[] args)
    {
        fight.leaveSpectator(client);

        super.abort(args);
    }

    public boolean canSubAction(GameActionTypeEnum actionType)
    {
        return false;
    }
}
