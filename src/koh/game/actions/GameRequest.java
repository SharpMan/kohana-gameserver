package koh.game.actions;

import koh.game.actions.requests.GameBaseRequest;
import koh.game.entities.actors.IGameActor;

/**
 *
 * @author Neo-Craft
 */
public class GameRequest extends GameAction {

    public GameBaseRequest Request;

    public GameRequest(IGameActor actor, GameBaseRequest request) {
        super(GameActionTypeEnum.BASIC_REQUEST, actor);
        this.Request = request;
    }

    @Override
    public void endExecute() {
        try {
            super.endExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public void abort(Object[] Args) {
        this.Request.declin();
        try {
            super.abort(Args);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return this.Request.canSubAction(ActionType);
        //return true;
    }

}
