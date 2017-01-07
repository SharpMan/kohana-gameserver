package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.actions.GameMapMovement;
import koh.game.actions.GameTutorial;
import koh.game.controllers.PlayerController;
import koh.game.dao.DAO;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.SlaveFighter;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.*;
import koh.protocol.messages.game.context.roleplay.MapInformationsRequestMessage;
import koh.protocol.messages.game.inventory.items.ObjectDropMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.types.game.context.ActorOrientation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Neo-Craft
 */
public class ContextHandler {

    private static final Logger logger = LogManager.getLogger(ContextHandler.class);

    @HandlerAttribute(ID = GameMapChangeOrientationRequestMessage.M_ID)
    public static void handleGameMapChangeOrientationRequestMessage(WorldClient client, GameMapChangeOrientationRequestMessage message) {
        if(!client.canParsePacket(message.getClass().getName(), 900)){
            return;
        }
        if (client.getCharacter().getFight() == null) {
            client.getCharacter().setDirection(message.direction);
            client.getCharacter().getCurrentMap().sendToField(new GameMapChangeOrientationMessage(new ActorOrientation(client.getCharacter().getID(), client.getCharacter().getDirection())));
            logger.debug("New direction for actor {}~{}", client.getCharacter().getID(), message.direction);
        }
    }

    @HandlerAttribute(ID = ShowCellRequestMessage.M_ID)
    public static void handleShowCellRequestMessage(WorldClient client, ShowCellRequestMessage message) {
        if (client.getCharacter().getFighter() != null) {
            if(!client.canParsePacket(message.getClass().getName(), 800)){
                return;
            }
            client.getCharacter().getFighter().showCell(message.cellId, true);
        }
        //Spectator
    }

    @HandlerAttribute(ID = GameContextReadyMessage.M_ID)
    public static void handleGameContextReadyMessage(WorldClient client, GameContextReadyMessage message) {
        if (client.getCharacter().getFighter() != null && client.getCharacter().getFight() != null) {
            client.addGameAction(new GameFight(client.getCharacter().getFighter(), client.getCharacter().getFight()));
            client.send(client.getCharacter().getCurrentMap().getMapComplementaryInformationsDataMessage(client.getCharacter()));
            client.getCharacter().getFight().onReconnect(client.getCharacter().getFighter().asPlayer());
        }
    }

    @HandlerAttribute(ID = GameContextCreateRequestMessage.MESSAGE_ID)
    public static void handleGameContextCreateRequestMessage(WorldClient Client, Message message) {
        if (Client.getCharacter().isInWorld()) {
            PlayerController.sendServerMessage(Client, "You are already Logged !");
        } else {

            Client.sequenceMessage(new GameContextDestroyMessage());

            Client.send(new GameContextCreateMessage((byte) (Client.getCharacter().getFighter() == null ? 1 : 2)));
            Client.getCharacter().updateRegenedEnergy();
            Client.getCharacter().refreshStats(false, false);
            Client.getCharacter().onLogged();
        }
    }

    @HandlerAttribute(ID = ObjectDropMessage.MESSAGE_ID)
    public static void handleObjectDropMessage(WorldClient client, ObjectDropMessage message) {
        if(client.getCharacter() == null || client.isGameAction(GameActionTypeEnum.EXCHANGE)){
            return;
        }
        InventoryItem item = client.getCharacter().getInventoryCache().find(message.objectUID);
        if (item == null || item.getQuantity() < message.quantity) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP));
            return;
        } else if (item.getEffect(983) != null) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.NOT_TRADABLE));
            return;
        }
        if (item.getSlot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            client.getCharacter().getInventoryCache().unEquipItem(item);
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
            final DofusCell curcell = client.getCharacter().getCurrentMap().getCell(cellID);
            if (curcell.nonWalkableDuringRP() || client.getCharacter().getCurrentMap().cellIsOccuped(cellID) || client.getCharacter().getCurrentMap().hasActorOnCell(cellID)) {
                cellID = -1;
                continue;
            }
            break;
        }
        if (cellID == -1 || message.quantity <= 0) {
            client.send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP_NO_PLACE));
            return;
        }
        final int newQua = item.getQuantity() - message.quantity;
        if (newQua <= 0) {
            client.getCharacter().getInventoryCache().removeItemFromInventory(item);
            DAO.getItems().save(item, false, "character_items");
        } else {
            client.getCharacter().getInventoryCache().updateObjectquantity(item, newQua);
            item = CharacterInventory.tryCreateItem(item.getTemplateId(), null, message.quantity, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), item.getEffectsCopy());
        }

        client.getCharacter().getCurrentMap().addItem(cellID, item);
        client.send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = MapInformationsRequestMessage.MESSAGE_ID)
    public static void handleMapInformationsRequestMessage(WorldClient client, Message message) {
        //client.sequenceMessage();
        if (client.getCharacter().isOnTutorial()) {
            client.send(client.getCharacter().getCurrentMap().getFakedMapComplementaryInformationsDataMessage(client.getCharacter()));
            client.addGameAction(new GameTutorial(client.getCharacter()));
        } else
            client.send(client.getCharacter().getCurrentMap().getMapComplementaryInformationsDataMessage(client.getCharacter()));
        client.getCharacter().getCurrentMap().sendMapInfo(client);

    }

    @HandlerAttribute(ID = GameMapMovementCancelMessage.MESSAGE_ID)
    public static void handleGameMapMovementCancelMessage(WorldClient Client, GameMapMovementCancelMessage Message) {
        Client.abortGameAction(GameActionTypeEnum.MAP_MOVEMENT, new Object[]{Message.cellId});
        Client.endGameAction(GameActionTypeEnum.MAP_MOVEMENT);
    }

    @HandlerAttribute(ID = GameMapMovementConfirmMessage.MESSAGE_ID)
    public static void handleGameMapMovementConfirmMessage(WorldClient Client, GameMapMovementConfirmMessage message) {
        try {
            Client.endGameAction(GameActionTypeEnum.MAP_MOVEMENT);
            Client.send(new BasicNoOperationMessage());
            Client.getCharacter().getCurrentMap().onMouvementConfirmed(Client.getCharacter());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = GameMapMovementRequestMessage.MESSAGE_ID)
    public static void handleGameMapMovementRequestMessage(WorldClient client, GameMapMovementRequestMessage message) {

        if (message.keyMovements.length <= 0) {
            logger.error("Empty Path{}", client.getIP());
            client.send(new BasicNoOperationMessage());
            return;
        }

        if (client.isGameAction(GameActionTypeEnum.FIGHT) && client.getCharacter().getFighter() != null) {
            final Fighter mover = client.getCharacter().getFight().getCurrentFighter() instanceof SlaveFighter
                    && client.getCharacter().getFight().getCurrentFighter().getSummoner() == client.getCharacter().getFighter() ?
                    client.getCharacter().getFight().getCurrentFighter() : client.getCharacter().getFighter();
            final MovementPath path = Pathfunction.isValidPath(client.getCharacter().getFight(), mover, mover.getCellId(), mover.getDirection(), message.keyMovements);
            if (path != null) {
                if (mover .isDead()) {
                    client.send(new BasicNoOperationMessage());
                    client.getCharacter().getFight().endTurn();
                    return;
                }
                final GameMapMovement GameMovement = client.getCharacter().getFight().tryMove(mover, path);

                if (GameMovement != null) {
                    GameMovement.execute();
                }
            }
            return;
        }
        if(client.getCharacter() == null){
            return;
        }
        if (client.getCharacter().getCurrentMap() == null) {
            client.send(new GameMapNoMovementMessage());
            PlayerController.sendServerErrorMessage(client, "Votre map est absente veuillez le signalez au staff ");
            logger.error("Vacant map {} ", client.getCharacter().toString());
            return;
        }
        client.getCharacter().stopSitEmote();
        if (!client.canGameAction(GameActionTypeEnum.MAP_MOVEMENT)) {
            client.send(new GameMapNoMovementMessage());
            if(client.isGameAction(GameActionTypeEnum.TUTORIAL)){
                if(client.getCharacter().getLevel() == 1)
                    client.send(new PopupWarningMessage((byte) 4, "Melan", GameTutorial.LEVEL));
                else
                    client.send(new PopupWarningMessage((byte) 1, "Melan", "Eh petit, t'es paralizé.\nEcoute moi bon sang !"));
                return;
            }
            client.send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 12, new String[0]));
            return;
        }
        client.addGameAction(new GameMapMovement(client.getCharacter().getCurrentMap(), client.getCharacter(), message.keyMovements));
        client.getCharacter().getCurrentMap().sendToField(new GameMapMovementMessage(message.keyMovements, client.getCharacter().getID()));
    }

}
