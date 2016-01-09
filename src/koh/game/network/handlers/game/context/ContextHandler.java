package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.actions.GameMapMovement;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.character.stats.UpdateLifePointsMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextCreateRequestMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.GameContextReadyMessage;
import koh.protocol.messages.game.context.GameMapChangeOrientationMessage;
import koh.protocol.messages.game.context.GameMapChangeOrientationRequestMessage;
import koh.protocol.messages.game.context.GameMapMovementCancelMessage;
import koh.protocol.messages.game.context.GameMapMovementConfirmMessage;
import koh.protocol.messages.game.context.GameMapMovementMessage;
import koh.protocol.messages.game.context.GameMapMovementRequestMessage;
import koh.protocol.messages.game.context.GameMapNoMovementMessage;
import koh.protocol.messages.game.context.ShowCellRequestMessage;
import koh.protocol.messages.game.context.roleplay.MapInformationsRequestMessage;
import koh.protocol.messages.game.inventory.items.ObjectDropMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;
import koh.protocol.types.game.context.ActorOrientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class ContextHandler {

    private static final Logger logger = LogManager.getLogger(ContextHandler.class);

    @HandlerAttribute(ID = GameMapChangeOrientationRequestMessage.M_ID)
    public static void HandleGameMapChangeOrientationRequestMessage(WorldClient Client, GameMapChangeOrientationRequestMessage Message) {
        if (Client.getCharacter().getFight() == null) {
            Client.getCharacter().setDirection(Message.direction);
            Client.getCharacter().getCurrentMap().sendToField(new GameMapChangeOrientationMessage(new ActorOrientation(Client.getCharacter().getID(), Client.getCharacter().getDirection())));
            logger.debug("New direction for actor {}~{}" , Client.getCharacter().getID() , Message.direction);
        }
    }

    @HandlerAttribute(ID = ShowCellRequestMessage.M_ID)
    public static void HandleShowCellRequestMessage(WorldClient Client, ShowCellRequestMessage Message) {
        if (Client.getCharacter().getFighter() != null) {
            Client.getCharacter().getFighter().showCell(Message.cellId, true);
        }
        //Spectator
    }

    @HandlerAttribute(ID = GameContextReadyMessage.M_ID)
    public static void HandleGameContextReadyMessage(WorldClient client, GameContextReadyMessage Message) {
        if (client.getCharacter().getFighter() != null) {
            client.addGameAction(new GameFight(client.getCharacter().getFighter(), client.getCharacter().getFight()));
            client.send(client.getCharacter().getCurrentMap().getMapComplementaryInformationsDataMessage(client.getCharacter()));
            client.getCharacter().getFight().onReconnect((CharacterFighter) client.getCharacter().getFighter());
        }
    }

    @HandlerAttribute(ID = GameContextCreateRequestMessage.MESSAGE_ID)
    public static void HandleGameContextCreateRequestMessage(WorldClient Client, Message message) {
        if (Client.getCharacter().isInWorld()) {
            PlayerController.sendServerMessage(Client, "You are already Logged !");
        } else {
            Client.sequenceMessage(new GameContextDestroyMessage());

            Client.send(new GameContextCreateMessage((byte) (Client.getCharacter().getFighter() == null ? 1 : 2)));
            Client.getCharacter().updateRegenedEnergy();
            Client.getCharacter().refreshStats(false,false);
            Client.getCharacter().onLogged();
        }
    }

    @HandlerAttribute(ID = ObjectDropMessage.MESSAGE_ID)
    public static void HandleObjectDropMessage(WorldClient client, ObjectDropMessage Message) {
        InventoryItem Item = client.getCharacter().getInventoryCache().find(Message.objectUID);
        if (Item == null || Item.getQuantity() < Message.quantity) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP));
            return;
        } else if (Item.getEffect(983) != null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.NOT_TRADABLE));
            return;
        }
        if (Item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            client.getCharacter().getInventoryCache().unEquipItem(Item);
            client.getCharacter().refreshStats();
        }

        short cellID = -1;
        for (int a = 0; a < 1; ++a) // TODO: getNearestFreeCell
        {
            switch (a) {
                case 0:
                    cellID = (short) (client.getCharacter().getCell().getId() - 14);
                    break;
                case 1:
                    cellID = (short) (client.getCharacter().getCell().getId() - 14 + 1);
                    break;
                case 2:
                    cellID = (short) (client.getCharacter().getCell().getId() + 14 - 1);
                    break;
                case 3:
                    cellID = (short) (client.getCharacter().getCell().getId() + 14);
                    break;
            }
            DofusCell curcell = client.getCharacter().getCurrentMap().getCell(cellID);
            if (curcell.nonWalkableDuringRP() || client.getCharacter().getCurrentMap().cellIsOccuped(cellID) || client.getCharacter().getCurrentMap().hasActorOnCell(cellID)) {
                cellID = -1;
                continue;
            }
            break;
        }
        if (cellID == -1 || Message.quantity <= 0) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP_NO_PLACE));
            return;
        }
        int newQua = Item.getQuantity() - Message.quantity;
        if (newQua <= 0) {
            client.getCharacter().getInventoryCache().removeItemFromInventory(Item);
            DAO.getItems().save(Item, false, "character_items");
        } else {
            client.getCharacter().getInventoryCache().updateObjectquantity(Item, newQua);
            Item = CharacterInventory.tryCreateItem(Item.getTemplateId(), null, Message.quantity, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy());
        }

        client.getCharacter().getCurrentMap().addItem(cellID, Item);
        client.send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = MapInformationsRequestMessage.MESSAGE_ID)
    public static void HandleMapInformationsRequestMessage(WorldClient Client, Message message) {
        //client.sequenceMessage();
        Client.send(Client.getCharacter().getCurrentMap().getMapComplementaryInformationsDataMessage(Client.getCharacter()));
        Client.getCharacter().getCurrentMap().sendMapInfo(Client);

    }

    @HandlerAttribute(ID = GameMapMovementCancelMessage.MESSAGE_ID)
    public static void HandleGameMapMovementCancelMessage(WorldClient Client, GameMapMovementCancelMessage Message) {
        Client.abortGameAction(GameActionTypeEnum.MAP_MOVEMENT, new Object[]{Message.cellId});
        Client.endGameAction(GameActionTypeEnum.MAP_MOVEMENT);
    }

    @HandlerAttribute(ID = GameMapMovementConfirmMessage.MESSAGE_ID)
    public static void HandleGameMapMovementConfirmMessage(WorldClient Client, GameMapMovementConfirmMessage message) {
        try {
            Client.endGameAction(GameActionTypeEnum.MAP_MOVEMENT);
            Client.send(new BasicNoOperationMessage());
            Client.getCharacter().getCurrentMap().onMouvementConfirmed(Client.getCharacter());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = GameMapMovementRequestMessage.MESSAGE_ID)
    public static void HandleGameMapMovementRequestMessage(WorldClient client, GameMapMovementRequestMessage message) {

        if (message.keyMovements.length <= 0) {
            logger.error("Empty Path{}" , client.getIP());
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.isGameAction(GameActionTypeEnum.FIGHT)) {
            MovementPath Path = Pathfinder.isValidPath(client.getCharacter().getFight(), client.getCharacter().getFighter(), client.getCharacter().getFighter().getCellId(), client.getCharacter().getFighter().getDirection(), message.keyMovements);
            if (Path != null) {
                if (client.getCharacter().getFighter().isDead()) {
                    client.send(new BasicNoOperationMessage());
                    client.getCharacter().getFight().endTurn();
                    return;
                }
                GameMapMovement GameMovement = client.getCharacter().getFight().tryMove(client.getCharacter().getFighter(), Path);

                if (GameMovement != null) {
                    GameMovement.execute();
                }
            }
            return;
        }
        if (client.getCharacter().getCurrentMap() == null) {
            client.send(new GameMapNoMovementMessage());
            PlayerController.SendServerErrorMessage(client, "Votre map est absente veuillez le signalez au staff ");
            logger.error("Vacant map {} " , client.getCharacter().toString());
            return;
        }
        client.getCharacter().stopSitEmote();
        if (!client.canGameAction(GameActionTypeEnum.MAP_MOVEMENT)) {
            client.send(new GameMapNoMovementMessage());
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 12, new String[0]));
            return;
        }
        client.addGameAction(new GameMapMovement(client.getCharacter().getCurrentMap(), client.getCharacter(), message.keyMovements));
        client.getCharacter().getCurrentMap().sendToField(new GameMapMovementMessage(message.keyMovements, client.getCharacter().getID()));
    }

}
