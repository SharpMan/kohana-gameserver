package koh.game.network.handlers.game.context;

import koh.game.network.WorldClient;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.messages.game.idol.IdolListMessage;
import koh.protocol.messages.game.idol.IdolPartyRegisterRequestMessage;
import koh.protocol.types.game.idol.PartyIdol;

/**
 *
 * @author Neo-Craft
 */
public class IdoleHandler {

    @HandlerAttribute(ID = IdolPartyRegisterRequestMessage.M_ID)
    public static void HandleIdolPartyRegisterRequestMessage(WorldClient Client, IdolPartyRegisterRequestMessage Message) {
        Client.Send(new IdolListMessage(new int[0], new int[0], new PartyIdol[0]));
    }

}
