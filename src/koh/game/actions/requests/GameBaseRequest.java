package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;

/**
 *
 * @author Neo-Craft
 */
public abstract class GameBaseRequest {

    public abstract boolean CanSubAction(GameActionTypeEnum Action);

    public WorldClient Requester;

    public WorldClient Requested;

    public boolean IsFinish;

    public GameBaseRequest(WorldClient Requester, WorldClient Requested) {
        this.Requester = Requester;
        this.Requested = Requested;
    }

    public boolean Accept() {
        if (this.IsFinish) {
            return false;
        }

        this.IsFinish = true;

        return true;
    }

    public boolean Declin() {
        if (this.IsFinish) {
            return false;
        }
        this.IsFinish = true;

        return true;
    }

}
