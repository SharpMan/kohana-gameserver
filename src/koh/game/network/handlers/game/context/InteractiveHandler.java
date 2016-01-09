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
    public static void HandleInteractiveUseRequestMessage(WorldClient client, InteractiveUseRequestMessage Message) {
        if (!client.canGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT)) {
            client.send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }
        InteractiveElementStruct Element = client.getCharacter().getCurrentMap().getInteractiveElementStruct(Message.elemId);
        if (Element == null) {
            client.send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }
        InteractiveElementSkill Skill = Element.getSkill(Message.skillInstanceUid);
        if (Skill == null) {
            client.send(new InteractiveUseErrorMessage(Message.elemId, Message.skillInstanceUid));
            return;
        }

        client.addGameAction(new InteractiveElementAction(client.getCharacter(), Skill, Message.elemId));

        try {
            if (InteractiveElementAction.HANDLERS.get(InteractiveActionEnum.valueOf(Skill.skillId)) != null && InteractiveElementAction.HANDLERS.get(InteractiveActionEnum.valueOf(Skill.skillId)).getDuration() == 0) {
                client.endGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @HandlerAttribute(ID = InteractiveUseEndedMessage.MESSAGE_ID)
    public static void HandleInteractiveUseEndedMessage(WorldClient Client, InteractiveUseEndedMessage Message) {
        try {
            Client.endGameAction(GameActionTypeEnum.INTERACTIVE_ELEMENT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        throw new Error("Received " + Message.toString());
    }

    @HandlerAttribute(ID = TeleportRequestMessage.MESSAGE_ID)
    public static void HandleTeleportRequestMessage(WorldClient Client, TeleportRequestMessage Message) {
        if (!Client.isGameAction(GameActionTypeEnum.ZAAP)) {
            Client.send(new BasicNoOperationMessage());
            return;
        }
        Client.abortGameAction(GameActionTypeEnum.ZAAP, new Object[]{Message.mapId});
        Client.delGameAction(GameActionTypeEnum.ZAAP);
    }

}
