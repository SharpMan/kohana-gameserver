package koh.game.actions.requests;

import koh.game.actions.GameActionTypeEnum;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeTypeEnum;
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
    public boolean Accept() {
        return super.Accept();
    }

    @Override
    public boolean Declin() {
        if (!super.Declin()) {
            return false;
        }

        try {
            Message Message = new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, false);

            this.Requester.Send(Message);
            this.Requested.Send(Message);
            
            this.Requester.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
            this.Requested.EndGameAction(GameActionTypeEnum.BASIC_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.Requester.SetBaseRequest(null);
        this.Requested.SetBaseRequest(null);

        return true;
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum Action) {
        return false;
    }
}
