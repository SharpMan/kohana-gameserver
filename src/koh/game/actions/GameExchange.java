package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.game.exchange.Exchange;

/**
 *
 * @author Neo-Craft
 */
public class GameExchange extends GameAction {

    public Exchange Exchange;

    public GameExchange(IGameActor Actor, Exchange Exchange) {
        super(GameActionTypeEnum.EXCHANGE, Actor);
        this.Exchange = Exchange;
    }
    

    @Override
    public void EndExecute() {
        if (!Exchange.ExchangeFinish()) {
            Exchange.CloseExchange(true);
        }

        try {
            super.EndExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public void Abort(Object[] Args) {

        if (!Exchange.ExchangeFinish()) {
            Exchange.CloseExchange();
        }
        try {
            super.EndExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
