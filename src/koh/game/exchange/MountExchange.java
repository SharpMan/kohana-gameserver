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
        this.send(new ExchangeMountsStableAddMessage(new MountClientData[0]));
        this.send(new ExchangeStartOkMountWithOutPaddockMessage(new MountClientData[0]));
        //this.send(new BasicStatMessage(CLICK_ON_BUTTON));
    }

    @Override
    public boolean transfertAllToInv(WorldClient client, InventoryItem[] items) {
        return false;
    }

    @Override
    public boolean moveItems(WorldClient client, InventoryItem[] items, boolean add) {
        return false;
    }

    @Override
    public boolean moveItem(WorldClient client, int itemID, int quantity) {
        return false;
    }

    @Override
    public boolean moveKamas(WorldClient Client, int quantity) {
       return false;
    }

    @Override
    public boolean buyItem(WorldClient client, int templateId, int quantity) {
        return false;
    }

    @Override
    public boolean sellItem(WorldClient client, InventoryItem item, int quantity) {
        return false;
    }

    @Override
    public boolean validate(WorldClient client) {
        return false;
    }

    @Override
    public boolean finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean closeExchange(boolean success) {
        this.finish();
        this.myClient.setMyExchange(null);
        this.send(new ExchangeLeaveMessage(DialogTypeEnum.DIALOG_EXCHANGE, success));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void send(Message packet) {
        this.myClient.send(packet);
    }

}
