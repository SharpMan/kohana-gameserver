package koh.game.exchange;

import com.google.common.primitives.Ints;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.Npc;
import static koh.game.entities.actors.character.CharacterInventory.UnMergeableType;
import koh.game.entities.actors.npc.NpcItem;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeErrorEnum;
import koh.protocol.client.enums.ItemSuperTypeEnum;
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
    public boolean MoveItem(WorldClient Client, int ItemID, int Quantity) {

        return false;
    }

    @Override
    public boolean MoveKamas(WorldClient Client, int Quantity) {
        return false;
    }

    @Override
    public boolean BuyItem(WorldClient Client, int TemplateId, int Quantity) {
        if (this.myEnd) // ne devrait jamais arriver
        {
            return false;
        }

        NpcItem npcItem = this.Npc.Template().Items.get(TemplateId);

        if (npcItem == null) {
            Client.Send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }
        if((npcItem.Template().realWeight * Quantity) + Client.Character.InventoryCache.Weight() > Client.Character.InventoryCache.WeightTotal()){
            PlayerController.SendServerErrorMessage(Client, "Erreur : Votre poids depasse les bornes...");
            return false;
        }

        if (Ints.contains(UnMergeableType, ItemDAO.Cache.get(TemplateId).TypeId)) {
            Quantity = 1;
        }

        int amount1 = (int) ((double) npcItem.Price() * (double) Quantity);

        InventoryItem playerItem = null;
        
        if (npcItem.ItemToken() != null) {
            playerItem = Client.Character.InventoryCache.GetItemInTemplate(npcItem.Token);
            if (playerItem == null || (double) playerItem.GetQuantity() < amount1) {
                return false;
            }
        } else if ((double) this.myClient.Character.Kamas < amount1) {
            Client.Send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }

        this.myClient.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 21, new String[]{Quantity + "", TemplateId + ""}));

        if (playerItem != null) {
            Client.Character.InventoryCache.UpdateObjectquantity(playerItem, playerItem.GetQuantity() - amount1);
        } else {
            Client.Character.InventoryCache.SubstractKamas(amount1);
        }

        InventoryItem Item = InventoryItem.Instance(ItemDAO.NextID++, TemplateId, 63, Client.Character.ID, Quantity, EffectHelper.GenerateIntegerEffect(ItemDAO.Cache.get(TemplateId).possibleEffects, npcItem.GenType(), ItemDAO.Cache.get(TemplateId) instanceof Weapon));
        if (this.myClient.Character.InventoryCache.Add(Item, true)) {
            Item.NeedInsert = true;
        }

        Client.Send(new ExchangeBuyOkMessage());

        return true;
    }

    @Override
    public boolean SellItem(WorldClient Client, InventoryItem Item, int Quantity) {
        if (this.myEnd) {
            return false;
        }

        if (Item == null) {
            Client.Send(new ExchangeErrorMessage(ExchangeErrorEnum.SELL_ERROR));
            return false;
        }

        NpcItem npcItem = this.Npc.Template().Items.get(Item.TemplateId);

        int Refund = npcItem == null ? (int) ((long) (int) Math.ceil((double) Item.Template().price / 10.0) * (long) Quantity) : (int) ((long) (int) Math.ceil((double) npcItem.Price() / 10.0) * (long) Quantity);
        if (Quantity == Item.GetQuantity()) {
            Client.Character.InventoryCache.RemoveItem(Item);
        } else {
            Client.Character.InventoryCache.UpdateObjectquantity(Item, Item.GetQuantity() - Quantity);
        }

        this.myClient.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{Quantity + "", Item.TemplateId + ""}));

        Client.Character.InventoryCache.AddKamas(Refund);

        return true;
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
        this.myClient.Send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.EndGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void Send(Message Packet) {
        this.myClient.Send(Packet);
    }

    @Override
    public boolean MoveItems(WorldClient Client, InventoryItem[] Items, boolean Add) {
        return false;
    }

    @Override
    public boolean TransfertAllToInv(WorldClient Client, InventoryItem[] Items) {
        return false;
    }

}
