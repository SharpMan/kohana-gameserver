package koh.game.actions.interactive;

import koh.game.entities.actors.Player;
import koh.protocol.client.enums.ItemSuperTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkCraftMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkCraftWithInformationMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkMulticraftCrafterMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeStartOkRunesTradeMessage;

/**
 * Created by Melancholia on 3/5/17.
 */
public class UpgradeItem implements InteractiveAction {

    private final ItemSuperTypeEnum itemType;
    private final int skill;

    public UpgradeItem(ItemSuperTypeEnum itemType, int skill) {
        this.itemType = itemType;
        this.skill = skill;
    }

    @Override
    public boolean isEnabled(Player actor) {
        return true;
    }

    @Override
    public void execute(Player actor, int element) {
        if (!this.isEnabled(actor)) {
            actor.send(new BasicNoOperationMessage());
            return;
        }
        actor.send(new ExchangeStartOkCraftWithInformationMessage(168)); //119
//        actor.send(new ExchangeStartOkCraftMessage());
        //actor.send(new ExchangeStartOkMulticraftCrafterMessage(/*(byte)4,*/169));
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public void leave(Player player, int element) {

    }

    @Override
    public void abort(Player player, int element) {

    }
}
