package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;

/**
 *
 * @author Neo-Craft
 */
public abstract class GameBaseRequest {

    public abstract boolean canSubAction(GameActionTypeEnum action);

    public WorldClient requester;

    public WorldClient requested;

    public boolean isFinish;

    public GameBaseRequest(WorldClient requester, WorldClient requested) {
        this.requester = requester;
        this.requested = requested;
    }

    public boolean accept() {
        if (this.isFinish) {
            return false;
        }

        this.isFinish = true;

        return true;
    }

    public boolean declin() {
        if (this.isFinish) {
            return false;
        }
        this.isFinish = true;

        return true;
    }

}
