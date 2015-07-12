package koh.game.actions;

import koh.game.actions.requests.GameBaseRequest;
import koh.game.entities.actors.IGameActor;

/**
 *
 * @author Neo-Craft
 */
public class GameRequest extends GameAction {

    public GameBaseRequest Request;

    public GameRequest(IGameActor Actor, GameBaseRequest Request) {
        super(GameActionTypeEnum.BASIC_REQUEST, Actor);
        this.Request = Request;
    }

    @Override
    public void EndExecute() {
        try {
            super.EndExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public void Abort(Object[] Args) {
        this.Request.Declin();
        try {
            super.Abort(Args);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return this.Request.CanSubAction(ActionType);
        //return true;
    }

}
