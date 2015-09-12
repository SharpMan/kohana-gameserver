package koh.game.network.handlers.game.context;

import koh.game.actions.GameActionTypeEnum;
import koh.game.actions.InteractiveElementAction;
import koh.game.entities.environments.InteractiveElementStruct;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.enums.InteractiveActionEnum;
import koh.protocol.messages.connection.BasicNoOperationMessage;
import koh.protocol.messages.game.interactive.InteractiveUseEndedMessage;
import koh.protocol.messages.game.interactive.InteractiveUseErrorMessage;
import koh.protocol.messages.game.interactive.InteractiveUseRequestMessage;
import koh.protocol.types.game.interactive.InteractiveElementSkill;
import koh.protocol.messages.game.interactive.zaap.TeleportRequestMessage;

/**
 *
 * @author Neo-Craft
 */
public class InteractiveHandler {

    @HandlerAttribute(ID = InteractiveUseRequestMessage.MESSAGE_ID)
    public static void HandleInteractiveUseRequestMessage(WorldClient Client, InteractiveUseRequestMessage Message) {
        if (!Client.CanGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT)) {
            Client.Send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }
        InteractiveElementStruct Element = Client.Character.CurrentMap.GetInteractiveElementStruct(Message.elemId);
        if (Element == null) {
            Client.Send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }
        InteractiveElementSkill Skill = Element.GetSkill(Message.skillInstanceUid);
        if (Skill == null) {
            Client.Send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }

        Client.AddGameAction(new InteractiveElementAction(Client.Character, Skill, Message.elemId));

        try {
            if (InteractiveElementAction.Handlers.get(InteractiveActionEnum.valueOf(Skill.skillId)) != null && InteractiveElementAction.Handlers.get(InteractiveActionEnum.valueOf(Skill.skillId)).GetDuration() == 0) {
                Client.EndGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = InteractiveUseEndedMessage.MESSAGE_ID)
    public static void HandleInteractiveUseEndedMessage(WorldClient Client, InteractiveUseEndedMessage Message) {
        try {
            Client.EndGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        throw new Error("Received " + Message.toString());
    }

    @HandlerAttribute(ID = TeleportRequestMessage.MESSAGE_ID)
    public static void HandleTeleportRequestMessage(WorldClient Client, TeleportRequestMessage Message) {
        if (!Client.IsGameAction(GameActionTypeEnum.ZAAP)) {
            Client.Send(new BasicNoOperationMessage());
            return;
        }
        Client.AbortGameAction(GameActionTypeEnum.ZAAP, new Object[]{Message.mapId});
        Client.DelGameAction(GameActionTypeEnum.ZAAP);
    }

}
