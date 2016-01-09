package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.inventory.exchanges.ExchangeLeaveMessage;

/**
 *
 * @author Neo-Craft
 */
public class ExchangeRequest extends GameBaseRequest {

    public ExchangeRequest(WorldClient Client, WorldClient Target) {
        super(Client, Target);
    }

    @Override
    public boolean accept() {
        return super.accept();
    }

    @Override
    public boolean declin() {
        if (!super.declin()) {
            return false;
        }

        try {
            Message Message = new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, false);

            this.requester.send(Message);
            this.requested.send(Message);
            
            this.requester.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.requested.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.requester.setBaseRequest(null);
        this.requested.setBaseRequest(null);

        return true;
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum action) {
        return false;
    }
}
