package koh.game.network.handlers.game.context;

import koh.game.Main;
import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.GameFight;
import koh.game.actions.GameMapMovement;
import koh.game.controllers.PlayerController;
import koh.game.dao.ItemDAO;
import koh.game.entities.actors.character.CharacterInventory;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.MovementPath;
import koh.game.entities.environments.Pathfinder;
import koh.game.entities.item.InventoryItem;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.network.handlers.game.approach.CharacterHandler;
import koh.protocol.client.Message;
import koh.protocol.client.enums.CharacterInventoryPositionEnum;
import koh.protocol.client.enums.ObjectErrorEnum;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.context.GameContextCreateMessage;
import koh.protocol.messages.game.context.GameContextCreateRequestMessage;
import koh.protocol.messages.game.context.GameContextDestroyMessage;
import koh.protocol.messages.game.context.GameContextReadyMessage;
import koh.protocol.messages.game.context.GameMapMovementCancelMessage;
import koh.protocol.messages.game.context.GameMapMovementConfirmMessage;
import koh.protocol.messages.game.context.GameMapMovementMessage;
import koh.protocol.messages.game.context.GameMapMovementRequestMessage;
import koh.protocol.messages.game.context.GameMapNoMovementMessage;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.protocol.messages.game.context.ShowCellRequestMessage;
import koh.protocol.messages.game.context.roleplay.MapInformationsRequestMessage;
import koh.protocol.messages.game.initialization.CharacterLoadingCompleteMessage;
import koh.protocol.messages.game.inventory.items.ObjectDropMessage;
import koh.protocol.messages.game.inventory.items.ObjectErrorMessage;

/**
 *
 * @author Neo-Craft
 */
public class ContextHandler {

    @HandlerAttribute(ID = ShowCellRequestMessage.M_ID)
    public static void HandleShowCellRequestMessage(WorldClient Client, ShowCellRequestMessage Message) {
        if (Client.Character.GetFighter() != null) {
            Client.Character.GetFighter().ShowCell(Message.cellId, true);
        }
        //Spectator
    }

    @HandlerAttribute(ID = GameContextReadyMessage.M_ID)
    public static void HandleGameContextReadyMessage(WorldClient Client, GameContextReadyMessage Message) {
        if (Client.Character.GetFighter() != null) {
            Client.AddGameAction(new GameFight(Client.Character.GetFighter(), Client.Character.GetFight()));
            Client.Send(Client.Character.CurrentMap.GetMapComplementaryInformationsDataMessage(Client.Character));
            Client.Character.GetFight().onReconnect((CharacterFighter) Client.Character.GetFighter());
        }
    }

    @HandlerAttribute(ID = GameContextCreateRequestMessage.MESSAGE_ID)
    public static void HandleGameContextCreateRequestMessage(WorldClient Client, Message message) {
        if (Client.Character.IsInWorld) {
            PlayerController.SendServerMessage(Client, "You are already Logged !");
        } else {
            Client.SequenceMessage(new GameContextDestroyMessage());

            Client.Send(new GameContextCreateMessage((byte) (Client.Character.GetFighter() == null ? 1 : 2)));
            Client.Character.RefreshStats(false);
            Client.Character.onLogged();
        }
    }

    @HandlerAttribute(ID = ObjectDropMessage.MESSAGE_ID)
    public static void HandleObjectDropMessage(WorldClient Client, ObjectDropMessage Message) {
        InventoryItem Item = Client.Character.InventoryCache.ItemsCache.get(Message.objectUID);
        if (Item == null || Item.GetQuantity() < Message.quantity) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP));
            return;
        } else if (Item.GetEffect(983) != null) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.NOT_TRADABLE));
            return;
        }
        if (Item.Slot() != CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED) {
            Client.Character.InventoryCache.UnEquipItem(Item);
            Client.Character.RefreshStats();
        }

        short cellID = -1;
        for (int a = 0; a < 1; ++a) // TODO: getNearestFreeCell
        {
            switch (a) {
                case 0:
                    cellID = (short) (Client.Character.Cell.Id - 14);
                    break;
                case 1:
                    cellID = (short) (Client.Character.Cell.Id - 14 + 1);
                    break;
                case 2:
                    cellID = (short) (Client.Character.Cell.Id + 14 - 1);
                    break;
                case 3:
                    cellID = (short) (Client.Character.Cell.Id + 14);
                    break;
            }
            DofusCell curcell = Client.Character.CurrentMap.getCell(cellID);
            if (curcell.NonWalkableDuringRP() || Client.Character.CurrentMap.CellIsOccuped(cellID) || Client.Character.CurrentMap.HasActorOnCell(cellID)) {
                cellID = -1;
                continue;
            }
            break;
        }
        if (cellID == -1 || Message.quantity <= 0) {
            Client.Send(new ObjectErrorMessage(ObjectErrorEnum.CANNOT_DROP_NO_PLACE));
            return;
        }
        int newQua = Item.GetQuantity() - Message.quantity;
        if (newQua <= 0) {
            Client.Character.InventoryCache.RemoveItemFromInventory(Item);
            ItemDAO.Update(Item, false, "character_items");
        } else {
            Client.Character.InventoryCache.UpdateObjectquantity(Item, newQua);
            Item = CharacterInventory.TryCreateItem(Item.TemplateId, null, Message.quantity, CharacterInventoryPositionEnum.INVENTORY_POSITION_NOT_EQUIPED.value(), Item.getEffectsCopy());
        }

        Client.Character.CurrentMap.AddItem(cellID, Item);
        Client.Send(new BasicNoOperationMessage());
    }

    @HandlerAttribute(ID = MapInformationsRequestMessage.MESSAGE_ID)
    public static void HandleMapInformationsRequestMessage(WorldClient Client, Message message) {
        //Client.SequenceMessage();
        Client.Send(Client.Character.CurrentMap.GetMapComplementaryInformationsDataMessage(Client.Character));
        Client.Character.CurrentMap.SendMapInfo(Client);

    }

    @HandlerAttribute(ID = GameMapMovementCancelMessage.MESSAGE_ID)
    public static void HandleGameMapMovementCancelMessage(WorldClient Client, GameMapMovementCancelMessage Message) {
        Client.AbortGameAction(GameActionTypeEnum.MAP_MOVEMENT, new Object[]{Message.cellId});
        Client.EndGameAction(GameActionTypeEnum.MAP_MOVEMENT);
    }

    @HandlerAttribute(ID = GameMapMovementConfirmMessage.MESSAGE_ID)
    public static void HandleGameMapMovementConfirmMessage(WorldClient Client, GameMapMovementConfirmMessage message) {
        try {
            Client.EndGameAction(GameActionTypeEnum.MAP_MOVEMENT);
            Client.Send(new BasicNoOperationMessage());
            Client.Character.CurrentMap.OnMouvementConfirmed(Client.Character);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = GameMapMovementRequestMessage.MESSAGE_ID)
    public static void HandleGameMapMovementRequestMessage(WorldClient Client, GameMapMovementRequestMessage Message) {

        if (Message.keyMovements.length <= 0) {
            Main.Logs().writeError("Empty Path" + Client.getIP());
            Client.Send(new BasicNoOperationMessage());
            return;
        }

        if (Client.IsGameAction(GameActionTypeEnum.FIGHT)) {
            MovementPath Path = Pathfinder.IsValidPath(Client.Character.GetFight(), Client.Character.GetFighter(), Client.Character.GetFighter().CellId(), Client.Character.GetFighter().Direction, Message.keyMovements);
            if (Path != null) {
                if (Client.Character.GetFighter().Dead()) {
                    Client.Send(new BasicNoOperationMessage());
                    Client.Character.GetFight().EndTurn();
                    return;
                }

                /*GameMapMovement GameMovement = */
                Client.Character.GetFight().TryMove(Client.Character.GetFighter(), Path).Execute();
                /*if (GameMovement != null) {
                 Client.AddGameAction(GameMovement);
                 }*/
            }
            return;
        }
        if (!Client.CanGameAction(GameActionTypeEnum.MAP_MOVEMENT)) {
            Client.Send(new GameMapNoMovementMessage());
            Client.Send(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 12, new String[0]));
            return;
        }
        Client.AddGameAction(new GameMapMovement(Client.Character.CurrentMap, Client.Character, Message.keyMovements));
        Client.Character.CurrentMap.sendToField(new GameMapMovementMessage(Message.keyMovements, Client.Character.ID));
    }

}
