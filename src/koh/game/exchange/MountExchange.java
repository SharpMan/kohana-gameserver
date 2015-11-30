package koh.game.exchange;

import koh.game.actions.GameActionTypeEnum;
import koh.game.entities.item.InventoryItem;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.messages.game.inventory.exchanges.ExchangeLeaveMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeMountsStableAddMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkMountWithOutPaddockMessage;
import koh.protocol.types.game.mount.MountClientData;

/**
 *
 * @author Neo-Craft
 */
public class MountExchange extends Exchange {

    private final WorldClient myClient;

    public MountExchange(WorldClient Client) {
        this.myClient = Client;
        this.Send(new ExchangeMountsStableAddMessage(new MountClientData[0]));
        this.Send(new ExchangeStartOkMountWithOutPaddockMessage(new MountClientData[0]));
        //this.send(new BasicStatMessage(CLICK_ON_BUTTON));
    }

    @Override
    public boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items) {
        return false;
    }

    @Override
    public boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add) {
        return false;
    }

    @Override
    public boolean MoveItem(WorldClient Client, int ItemID, int Quantity) {
        return false;
    }

    @Override
    public boolean MoveKamas(WorldClient Client, int Quantity) {
       return false;
    }

    @Override
    public boolean BuyItem(WorldClient Client, int TemplateId, int Quantity) {
        return false;
    }

    @Override
    public boolean SellItem(WorldClient Client, InventoryItem Item, int Quantity) {
        return false;
    }

    @Override
    public boolean Validate(WorldClient Client) {
        return false;
    }

    @Override
    public boolean Finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean CloseExchange(boolean Success) {
        this.Finish();
        this.myClient.myExchange = null;
        this.Send(new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, Success));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void Send(Message Packet) {
        this.myClient.send(Packet);
    }

}
