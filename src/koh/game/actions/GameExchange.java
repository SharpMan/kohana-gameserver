package koh.game.actions;

import koh.game.entities.actors.IGameActor;
import koh.game.exchange.Exchange;

/**
 *
 * @author Neo-Craft
 */
public class GameExchange extends GameAction {

    public Exchange exchange;

    public GameExchange(IGameActor actor, Exchange exchange) {
        super(GameActionTypeEnum.EXCHANGE, actor);
        this.exchange = exchange;
    }
    

    @Override
    public void endExecute() {
        if (!exchange.exchangeFinish()) {
            exchange.closeExchange(true);
        }

        try {
            super.endExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public void abort(Object[] Args) {

        if (!exchange.exchangeFinish()) {
            exchange.closeExchange();
        }
        try {
            super.endExecute();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        return false;
    }

}
