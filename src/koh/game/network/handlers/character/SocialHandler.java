package koh.game.network.handlers.character;

import koh.game.dao.mysql.PlayerDAOImpl;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.game.utils.Settings;
import koh.protocol.messages.game.social.*;
import koh.protocol.messages.handshake.ProtocolRequired;

/**
 *
 * @author Neo-Craft
 */
public class SocialHandler {
    
    
    @HandlerAttribute(ID = 1)
    public static void HandleProtocolRequiredMessage(WorldClient Client , ProtocolRequired Message){
        Client.send(new ProtocolRequired(Settings.GetIntElement("Protocol.requiredVersion"), Settings.GetIntElement("Protocol.currentVersion")));
    }

    @HandlerAttribute(ID = ContactLookRequestByIdMessage.MESSAGE_ID)
    public static void HandleContactLookRequestByIdMessage(WorldClient Client, ContactLookRequestByIdMessage Message) {
        Player Target = PlayerDAOImpl.getCharacter(Message.playerId);
        if (Target == null || Target.getEntityLook() == null) {
            Client.send(new ContactLookErrorMessage(Message.requestId));
        } else {
            Client.send(new ContactLookMessage(Message.requestId, Target.nickName, Message.playerId, Target.getEntityLook()));
        }
    }

}
