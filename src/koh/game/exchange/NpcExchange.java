package koh.game.exchange;

import com.google.common.primitives.Ints;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.mysql.ItemTemplateDAOImpl;
import koh.game.entities.actors.Npc;
import static koh.game.entities.actors.character.CharacterInventory.unMergeableType;
import koh.game.entities.actors.npc.NpcItem;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeBuyOkMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeErrorMessage;

/**
 *
 * @author Neo-Craft
 */
public class NpcExchange extends Exchange {

    private final WorldClient myClient;
    public Npc Npc;

    public NpcExchange(WorldClient Client, Npc Npc) {
        this.myClient = Client;
        this.Npc = Npc;
    }

    @Override
    public boolean moveItem(WorldClient Client, int itemID, int quantity) {

        return false;
    }

    @Override
    public boolean moveKamas(WorldClient Client, int quantity) {
        return false;
    }

    @Override
    public boolean buyItem(WorldClient Client, int templateId, int quantity) {
        if (this.myEnd) // ne devrait jamais arriver
        {
            return false;
        }

        NpcItem npcItem = this.Npc.getTemplate().Items.get(templateId);

        if (npcItem == null) {
            Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }
        if((npcItem.getTemplate().realWeight * quantity) + Client.character.inventoryCache.getWeight() > Client.character.inventoryCache.getTotalWeight()){
            PlayerController.SendServerErrorMessage(Client, "Erreur : Votre poids depasse les bornes...");
            return false;
        }

        if (Ints.contains(unMergeableType, ItemTemplateDAOImpl.Cache.get(templateId).TypeId)) {
            quantity = 1;
        }

        int amount1 = (int) ((double) npcItem.getPrice() * (double) quantity);

        InventoryItem playerItem = null;
        
        if (npcItem.getItemToken() != null) {
            playerItem = Client.character.inventoryCache.getItemInTemplate(npcItem.token);
            if (playerItem == null || (double) playerItem.getQuantity() < amount1) {
                return false;
            }
        } else if ((double) this.myClient.character.kamas < amount1) {
            Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }

        this.myClient.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 21, new String[]{quantity + "", templateId + ""}));

        if (playerItem != null) {
            Client.character.inventoryCache.updateObjectquantity(playerItem, playerItem.getQuantity() - amount1);
        } else {
            Client.character.inventoryCache.substractKamas(amount1);
        }

        InventoryItem Item = InventoryItem.getInstance(ItemTemplateDAOImpl.nextId++, templateId, 63, Client.character.ID, quantity, EffectHelper.generateIntegerEffect(ItemTemplateDAOImpl.Cache.get(templateId).possibleEffects, npcItem.genType(), ItemTemplateDAOImpl.Cache.get(templateId) instanceof Weapon));
        if (this.myClient.character.inventoryCache.add(Item, true)) {
            Item.needInsert = true;
        }

        Client.send(new ExchangeBuyOkMessage());

        return true;
    }

    @Override
    public boolean sellItem(WorldClient Client, InventoryItem item, int quantity) {
        if (this.myEnd) {
            return false;
        }

        if (item == null) {
            Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.SELL_ERROR));
            return false;
        }

        NpcItem npcItem = this.Npc.getTemplate().Items.get(item.templateId);

        int Refund = npcItem == null ? (int) ((long) (int) Math.ceil((double) item.getTemplate().price / 10.0) * (long) quantity) : (int) ((long) (int) Math.ceil((double) npcItem.getPrice() / 10.0) * (long) quantity);
        if (quantity == item.getQuantity()) {
            Client.character.inventoryCache.removeItem(item);
        } else {
            Client.character.inventoryCache.updateObjectquantity(item, item.getQuantity() - quantity);
        }

        this.myClient.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{quantity + "", item.templateId + ""}));

        Client.character.inventoryCache.addKamas(Refund);

        return true;
    }

    @Override
    public boolean validate(WorldClient Client) {
        return false;
    }

    @Override
    public boolean finish() {
        this.myEnd = true;

        return true;
    }

    @Override
    public boolean closeExchange(boolean Success) {
        this.finish();
        this.myClient.myExchange = null;
        this.myClient.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void send(Message Packet) {
        this.myClient.send(Packet);
    }

    @Override
    public boolean moveItems(WorldClient Client, InventoryItem[] items, boolean add) {
        return false;
    }

    @Override
    public boolean transfertAllToInv(WorldClient Client, InventoryItem[] items) {
        return false;
    }

}
