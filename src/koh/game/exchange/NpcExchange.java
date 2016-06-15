package koh.game.exchange;

import com.google.common.primitives.Ints;
import koh.game.actions.GameActionTypeEnum;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.Npc;
import static koh.game.entities.actors.character.CharacterInventory.UN_MERGEABLE_TYPE;
import koh.game.entities.actors.pnj.NpcItem;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.entities.item.actions.LearnSpell;
import koh.game.network.WorldClient;
import koh.protocol.client.Message;
import koh.protocol.client.enums.ActionIdEnum;
import koh.protocol.client.enums.DialogTypeEnum;
import koh.protocol.client.enums.ExchangeErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.dialog.LeaveDialogMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeBuyOkMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeErrorMessage;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;

/**
 *
 * @author Neo-Craft
 */
public class NpcExchange extends Exchange {

    private final WorldClient myClient;
    public Npc npc;

    public NpcExchange(WorldClient Client, Npc Npc) {
        this.myClient = Client;
        this.npc = Npc;
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
        if (this.myEnd) // ne devrait jamais arriver
        {
            return false;
        }

        NpcItem npcItem = this.npc.getTemplate().getItems().get(templateId);

        if (npcItem == null || quantity < 0) {
            client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }
        if((npcItem.getTemplate().getRealWeight() * quantity) + client.getCharacter().getInventoryCache().getWeight() > client.getCharacter().getInventoryCache().getTotalWeight()){
            PlayerController.sendServerErrorMessage(client, "Erreur : Votre poids depasse les bornes...");
            return false;
        }

        if (Ints.contains(UN_MERGEABLE_TYPE, DAO.getItemTemplates().getTemplate(templateId).getTypeId())) {
            quantity = 1;
        }

        final int amount1 = (int) ((double) npcItem.getPrice() * (double) quantity);

        InventoryItem playerItem = null;
        
        if (npcItem.getItemToken() != null) {
            playerItem = client.getCharacter().getInventoryCache().getItemInTemplate(npcItem.getToken());
            if (playerItem == null || (double) playerItem.getQuantity() < amount1) {
                return false;
            }
        } else if ((double) this.myClient.getCharacter().getKamas() < amount1) {
            client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_GUEST));
            return false;
        }

        this.myClient.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 21, new String[]{quantity + "", templateId + ""}));

        if (playerItem != null) {
            client.getCharacter().getInventoryCache().updateObjectquantity(playerItem, playerItem.getQuantity() - amount1);
        } else {
            client.getCharacter().getInventoryCache().substractKamas(amount1);
        }

        final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), templateId, 63, client.getCharacter().getID(), quantity, EffectHelper.generateIntegerEffect(DAO.getItemTemplates().getTemplate(templateId).getPossibleEffects(), npcItem.genType(), DAO.getItemTemplates().getTemplate(templateId) instanceof Weapon));

        if (this.myClient.getCharacter().getInventoryCache().add(item, true)) {
            item.setNeedInsert(true);
        }

        client.send(new ExchangeBuyOkMessage());

        return true;
    }

    @Override
    public boolean sellItem(WorldClient client, InventoryItem item, int quantity) {
        if (this.myEnd) {
            return false;
        }

        if (item == null || quantity < 0) {
            client.send(new ExchangeErrorMessage(ExchangeErrorEnum.SELL_ERROR));
            return false;
        }else if(this.npc.getTemplate() == null){
            logger.error("Npc {} template is null",this.npc.getNpcId());
            client.send(new ExchangeErrorMessage(ExchangeErrorEnum.SELL_ERROR));
            return false;
        }else if(this.npc.getTemplate().getItems() == null){
            logger.error("Npc {} template items is null",this.npc.getNpcId());
            client.send(new ExchangeErrorMessage(ExchangeErrorEnum.SELL_ERROR));
            return false;
        }

        final NpcItem npcItem = this.npc.getTemplate().getItems().get(item.getTemplateId());

        //final int refund = npcItem == null ? (int) ((long) (int) Math.ceil((double) item.getTemplate().getPrice() / 10.0) * (long) quantity) : (int) ((long) (int) Math.ceil((double) npcItem.getPrice() / 10.0) * (long) quantity);
        final int refund = 0;
        if (quantity == item.getQuantity()) {
            client.getCharacter().getInventoryCache().removeItem(item);
        } else {
            client.getCharacter().getInventoryCache().updateObjectquantity(item, item.getQuantity() - quantity);
        }

        this.myClient.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_MESSAGE, 22, new String[]{quantity + "", item.getTemplateId() + ""}));

        client.getCharacter().getInventoryCache().addKamas(refund);

        return true;
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
        this.myClient.send(new LeaveDialogMessage(DialogTypeEnum.DIALOG_EXCHANGE));
        this.myClient.endGameAction(GameActionTypeEnum.EXCHANGE);

        return true;
    }

    @Override
    public void send(Message packet) {
        this.myClient.send(packet);
    }

    @Override
    public boolean moveItems(WorldClient client, InventoryItem[] items, boolean add) {
        return false;
    }

    @Override
    public boolean transfertAllToInv(WorldClient client, InventoryItem[] items) {
        return false;
    }

}
