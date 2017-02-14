package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameExchange;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.ExchangeRequest;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.actors.TaxCollector;
import koh.game.entities.item.EffectHelper;
import koh.game.entities.item.InventoryItem;
import koh.game.entities.item.Weapon;
import koh.game.exchange.*;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.utils.SecureParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.AggressableStatusEnum;
import koh.protocol.client.enums.EffectGenerationType;
import koh.protocol.client.enums.ExchangeErrorEnum;
import koh.protocol.client.enums.ExchangeTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.inventory.exchanges.*;

/**
 *
 * @author Neo-Craft
 */
public class ExchangeHandler {

    @HandlerAttribute(ID = ExchangeReadyMessage.M_ID)
    public static void HandleExchangeReadyMessage(WorldClient client, ExchangeReadyMessage Message) {
        if (!client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            client.send(new BasicNoOperationMessage());

        }
        else
            client.getMyExchange().validate(client);
    }

    @HandlerAttribute(ID = ExchangeRequestOnTaxCollectorMessage.M_ID)
    public static void handleExchangeRequestOnTaxCollectorMessage(WorldClient client, ExchangeRequestOnTaxCollectorMessage message){
        if(client.getCharacter() == null || client.getCharacter().getCurrentMap() == null){
            return;
        }
        final TaxCollector taxCollector = (TaxCollector) client.getCharacter().getCurrentMap().getActor(message.taxCollectorId);
        if(taxCollector == null){
            client.send(new BasicNoOperationMessage());
            throw new Error("Le pnj " + message.taxCollectorId + " est absent");
        }
        if (client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            PlayerController.sendServerMessage(client, "You're always in a exchange...");
            return;
        }
        if (client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
            synchronized (taxCollector.get$mutex()){
                taxCollector.getGatheredItem().forEach((id,qua) -> {
                    final InventoryItem item = InventoryItem.getInstance(DAO.getItems().nextItemId(), id, 63, client.getCharacter().getID(), qua, EffectHelper.generateIntegerEffect(DAO.getItemTemplates().getTemplate(id).getPossibleEffects(), EffectGenerationType.NORMAL, DAO.getItemTemplates().getTemplate(id) instanceof Weapon));
                    if (client.getCharacter().getInventoryCache().add(item, true)) {
                        item.setNeedInsert(true);
                    }
                });
                PlayerController.sendServerMessage(client,"Vous avez recolté "+taxCollector.getBagSize()+" items");
                client.getCharacter().getInventoryCache().addKamas(taxCollector.getKamas(),true);
                taxCollector.getGatheredItem().clear();

                if(taxCollector.getHonor() > taxCollector.getGuild().playerStream().count()){
                    final int honor = (int) Math.abs((float)taxCollector.getHonor() / taxCollector.getGuild().playerStream().filter(p -> p.getPvPEnabled() == AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE).count());
                    taxCollector.getGuild().playerStream().filter(p -> p.getPvPEnabled() == AggressableStatusEnum.PvP_ENABLED_AGGRESSABLE).forEach(pl -> {
                        pl.addHonor(honor,true);
                        PlayerController.sendServerMessage(pl.getClient(), "Vous avez gagné "+honor+" points d'honneur suite à la recolte du percepteur par "+client.getCharacter());
                    });
                }
                else{
                    client.getCharacter().addHonor(taxCollector.getHonor(),true);
                }
            }
            client.getCharacter().getCurrentMap().destroyActor(taxCollector);
            taxCollector.getGuild().getTaxCollectors().remove(taxCollector);
            DAO.getTaxCollectors().remove(taxCollector.getIden(),taxCollector.getMapid());
        }

    }

    @HandlerAttribute(ID = ExchangeObjectTransfertListFromInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertListFromInvMessage(WorldClient Client, ExchangeObjectTransfertListFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().moveItems(Client, Exchange.getCharactersItems(Client.getCharacter(), Message.ids), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertExistingFromInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertExistingFromInvMessage(WorldClient Client, ExchangeObjectTransfertExistingFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().moveItems(Client, Exchange.getCharactersItems(Client.getCharacter()), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6184)
    public static void HandleExchangeObjectTransfertAllFromInvMessage(WorldClient Client, ExchangeObjectTransfertAllFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().moveItems(Client, Exchange.getCharactersItems(Client.getCharacter()), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6039) //TODO : Take just IDS
    public static void HandleExchangeObjectTransfertListToInvMessage(WorldClient Client, ExchangeObjectTransfertListToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().transfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertExistingToInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertExistingToInvMessage(WorldClient Client, ExchangeObjectTransfertExistingToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().transfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertAllToInvMessage.M_ID) //False
    public static void HandleExchangeObjectTransfertAllToInvMessage(WorldClient Client, ExchangeObjectTransfertAllToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().transfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5520)
    public static void HandleExchangeObjectMoveKamaMessage(WorldClient Client, ExchangeObjectMoveKamaMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            Client.getMyExchange().moveKamas(Client, SecureParser.Integer(Message.quantity));
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectMoveMessage.M_ID)
    public static void HandleExchangeObjectMoveMessage(WorldClient Client, ExchangeObjectMoveMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.getMyExchange().moveItem(Client, Message.objectUID, Message.quantity)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeAcceptMessage.M_ID)
    public static void HandleExchangeAcceptMessage(WorldClient Client, ExchangeAcceptMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.BASIC_REQUEST)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (!(Client.getBaseRequest() instanceof ExchangeRequest)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        if (Client == Client.getBaseRequest().requester) {
            Client.send(new BasicNoOperationMessage());
            return;
        }

        if (Client.getBaseRequest().accept()) {

            WorldClient Trader = Client.getBaseRequest().requester;

            try {
                Client.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
                Trader.endGameAction(GameActionTypeEnum.BASIC_REQUEST);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Client.setBaseRequest(null);
            Trader.setBaseRequest(null);

            PlayerExchange Exchange = new PlayerExchange(Client, Trader);
            GameExchange ExchangeAction = new GameExchange(Client.getCharacter(), Exchange);

            Client.setMyExchange(Exchange);
            Trader.setMyExchange(Exchange);

            Client.addGameAction(ExchangeAction);
            Trader.addGameAction(ExchangeAction);

            Exchange.Open();

            return;
        }
    }

    @HandlerAttribute(ID = ExchangePlayerRequestMessage.M_ID)
    public static void HandleExchangePlayerRequestMessage(WorldClient Client, ExchangePlayerRequestMessage Message) {
        if (Message.exchangeType == ExchangeTypeEnum.PLAYER_TRADE) {
            if (Client.getCharacter().getCurrentMap().getActor(Message.target) == null) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_TOOL_TOO_FAR));
                return;
            }
            if (!Client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
                return;
            }
            if (!(Client.getCharacter().getCurrentMap().getActor(Message.target) instanceof Player)) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
                return;
            }
            WorldClient Target = ((Player) Client.getCharacter().getCurrentMap().getActor(Message.target)).getClient();
            if (!Target.canGameAction(GameActionTypeEnum.BASIC_REQUEST) /*||
                     target.character.HasRestriction(RestrictionEnum.RESTRICTION_CANT_EXCHANGE) ||
                     Client.getCharacter().HasRestriction(RestrictionEnum.RESTRICTION_CANT_EXCHANGE)*/) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_OCCUPIED));
                return;
            }
            if (Target.getMyExchange() != null) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_OCCUPIED));
                return;
            }

            ExchangeRequest Request = new ExchangeRequest(Client, Target);
            GameRequest RequestAction = new GameRequest(Client.getCharacter(), Request);

            Client.addGameAction(RequestAction);
            Target.addGameAction(RequestAction);

            Client.setBaseRequest(Request);
            Target.setBaseRequest(Request);

            Message Message2 = new ExchangeRequestedTradeMessage(ExchangeTypeEnum.PLAYER_TRADE, Client.getCharacter().getID(), Target.getCharacter().getID());

            Client.send(Message2);
            Target.send(Message2);
        } else {
            Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
        }
    }

    @HandlerAttribute(ID = ExchangeBuyMessage.M_ID)
    public static void HandleExchangeBuyMessage(WorldClient Client, ExchangeBuyMessage Message) {
        if (Client.getMyExchange() == null || Message.quantity <= 0) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.getMyExchange().buyItem(Client, Message.objectToBuyId, SecureParser.ItemQuantity(Message.quantity));
    }

    @HandlerAttribute(ID = ExchangeSellMessage.M_ID)
    public static void HandleExchangeSellMessage(WorldClient Client, ExchangeSellMessage Message) {
        if (Client.getMyExchange() == null || Message.quantity <= 0) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.getMyExchange().sellItem(Client, Client.getCharacter().getInventoryCache().find(Message.objectToSellId), SecureParser.ItemQuantity(Message.quantity));
    }

}
