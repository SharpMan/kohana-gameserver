package koh.game.actions;

import koh.game.executors.WaitingQueue;
import koh.game.network.WorldClient;

/**
 * Created by Melancholia on 3/25/16.
 */
public class GameWaiting extends GameAction {

    private final WaitingQueue queue;

    public GameWaiting(WorldClient client) {
        super(GameActionTypeEnum.WAITING, null);

        this.queue = new WaitingQueue(client);
    }

    @Override
    public void abort(Object[] args){
        this.queue.abort();
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return true;
    }
}
