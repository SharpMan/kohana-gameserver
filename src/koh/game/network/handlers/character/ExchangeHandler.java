package koh.game.network.handlers.character;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameExchange;
import koh.game.actions.GameRequest;
import koh.game.actions.requests.ExchangeRequest;
import koh.game.entities.actors.Player;
import koh.game.exchange.*;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.utils.SecureParser;
import koh.protocol.client.Message;
import koh.protocol.client.enums.ExchangeErrorEnum;
import koh.protocol.client.enums.ExchangeTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeAcceptMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeBuyMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeErrorMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectMoveKamaMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectMoveMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertAllFromInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertAllToInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertExistingFromInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertExistingToInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertListFromInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeObjectTransfertListToInvMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangePlayerRequestMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeReadyMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeRequestedTradeMessage;
import koh.protocol.messages.game.inventory.exchanges.ExchangeSellMessage;

/**
 *
 * @author Neo-Craft
 */
public class ExchangeHandler {

    @HandlerAttribute(ID = ExchangeReadyMessage.M_ID)
    public static void HandleExchangeReadyMessage(WorldClient Client, ExchangeReadyMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            Client.send(new BasicNoOperationMessage());
        }
        Client.myExchange.Validate(Client);
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertListFromInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertListFromInvMessage(WorldClient Client, ExchangeObjectTransfertListFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.MoveItems(Client, Exchange.CharactersItems(Client.character, Message.ids), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertExistingFromInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertExistingFromInvMessage(WorldClient Client, ExchangeObjectTransfertExistingFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.MoveItems(Client, Exchange.CharactersItems(Client.character), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6184)
    public static void HandleExchangeObjectTransfertAllFromInvMessage(WorldClient Client, ExchangeObjectTransfertAllFromInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.MoveItems(Client, Exchange.CharactersItems(Client.character), true)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 6039) //TODO : Take just IDS
    public static void HandleExchangeObjectTransfertListToInvMessage(WorldClient Client, ExchangeObjectTransfertListToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.TransfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertExistingToInvMessage.M_ID)
    public static void HandleExchangeObjectTransfertExistingToInvMessage(WorldClient Client, ExchangeObjectTransfertExistingToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.TransfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectTransfertAllToInvMessage.M_ID) //False
    public static void HandleExchangeObjectTransfertAllToInvMessage(WorldClient Client, ExchangeObjectTransfertAllToInvMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.TransfertAllToInv(Client, null)) {
                Client.send(new BasicNoOperationMessage());
            }
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = 5520)
    public static void HandleExchangeObjectMoveKamaMessage(WorldClient Client, ExchangeObjectMoveKamaMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            Client.myExchange.MoveKamas(Client, SecureParser.Integer(Message.quantity));
        } else {
            Client.send(new BasicNoOperationMessage());
        }
    }

    @HandlerAttribute(ID = ExchangeObjectMoveMessage.M_ID)
    public static void HandleExchangeObjectMoveMessage(WorldClient Client, ExchangeObjectMoveMessage Message) {
        if (Client.isGameAction(GameActionTypeEnum.EXCHANGE)) {
            if (!Client.myExchange.MoveItem(Client, Message.objectUID, Message.quantity)) {
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
            GameExchange ExchangeAction = new GameExchange(Client.character, Exchange);

            Client.myExchange = (Exchange);
            Trader.myExchange = (Exchange);

            Client.addGameAction(ExchangeAction);
            Trader.addGameAction(ExchangeAction);

            Exchange.Open();

            return;
        }
    }

    @HandlerAttribute(ID = ExchangePlayerRequestMessage.M_ID)
    public static void HandleExchangePlayerRequestMessage(WorldClient Client, ExchangePlayerRequestMessage Message) {
        if (Message.exchangeType == ExchangeTypeEnum.PLAYER_TRADE) {
            if (Client.character.currentMap.getActor(Message.target) == null) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_TOOL_TOO_FAR));
                return;
            }
            if (!Client.canGameAction(GameActionTypeEnum.EXCHANGE)) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
                return;
            }
            if (!(Client.character.currentMap.getActor(Message.target) instanceof Player)) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
                return;
            }
            WorldClient Target = ((Player) Client.character.currentMap.getActor(Message.target)).client;
            if (!Target.canGameAction(GameActionTypeEnum.BASIC_REQUEST) /*||
                     Target.character.HasRestriction(RestrictionEnum.RESTRICTION_CANT_EXCHANGE) ||
                     client.character.HasRestriction(RestrictionEnum.RESTRICTION_CANT_EXCHANGE)*/) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_OCCUPIED));
                return;
            }
            if (Target.myExchange != null) {
                Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_CHARACTER_OCCUPIED));
                return;
            }

            ExchangeRequest Request = new ExchangeRequest(Client, Target);
            GameRequest RequestAction = new GameRequest(Client.character, Request);

            Client.addGameAction(RequestAction);
            Target.addGameAction(RequestAction);

            Client.setBaseRequest(Request);
            Target.setBaseRequest(Request);

            Message Message2 = new ExchangeRequestedTradeMessage(ExchangeTypeEnum.PLAYER_TRADE, Client.character.ID, Target.character.ID);

            Client.send(Message2);
            Target.send(Message2);
        } else {
            Client.send(new ExchangeErrorMessage(ExchangeErrorEnum.REQUEST_IMPOSSIBLE));
        }
    }

    @HandlerAttribute(ID = ExchangeBuyMessage.M_ID)
    public static void HandleExchangeBuyMessage(WorldClient Client, ExchangeBuyMessage Message) {
        if (Client.myExchange == null || Message.quantity <= 0) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.myExchange.BuyItem(Client, Message.objectToBuyId, SecureParser.ItemQuantity(Message.quantity));
    }

    @HandlerAttribute(ID = ExchangeSellMessage.M_ID)
    public static void HandleExchangeSellMessage(WorldClient Client, ExchangeSellMessage Message) {
        if (Client.myExchange == null || Message.quantity <= 0) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.myExchange.SellItem(Client, Client.character.inventoryCache.itemsCache.get(Message.objectToSellId), SecureParser.ItemQuantity(Message.quantity));
    }

}
