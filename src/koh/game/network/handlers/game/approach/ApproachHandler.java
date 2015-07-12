package koh.game.network.handlers.game.approach;

import koh.game.dao.AccountTicketDAO;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.game.network.handlers.HandlerAttribute;
import koh.protocol.client.Message;
import koh.protocol.messages.connection.LoginQueueStatusMessage;
import koh.protocol.messages.game.approach.AuthenticationTicketMessage;
import koh.protocol.messages.game.approach.AuthenticationTicketRefusedMessage;
import koh.protocol.messages.game.startup.StartupActionAddObject;
import koh.protocol.messages.game.startup.StartupActionsExecuteMessage;
import koh.protocol.messages.game.startup.StartupActionsListMessage;
import koh.protocol.messages.security.ClientKeyMessage;

/**
 *
 * @author Neo-Craft
 */
public class ApproachHandler {
    
    @HandlerAttribute(ID = StartupActionsExecuteMessage.M_ID)
    public static void HandleStartupActionsExecuteMessage(WorldClient Client , StartupActionsExecuteMessage Message){
        Client.Send(new StartupActionsListMessage(new StartupActionAddObject[0]));//Client.Send(Message);
    }

    @HandlerAttribute(ID = AuthenticationTicketMessage.MESSAGE_ID)
    public static void AuthenticationTicketMessage(WorldClient Client, Message message) {

        Client.tempTicket = AccountTicketDAO.getWaitingCompte(((AuthenticationTicketMessage) message).Ticket);

        if (Client.tempTicket != null && Client.tempTicket.isCorrect(Client.getIP(), ((AuthenticationTicketMessage) message).Ticket)) {
            WorldServer.Loader.addClient(Client);
            if (WorldServer.Loader.getPosition(Client) != 1) {
                Client.Send(new LoginQueueStatusMessage((short) WorldServer.Loader.getPosition(Client), (short) WorldServer.Loader.getTotal()));
                Client.showQueue = true;
            }
        } else {
            Client.Send(new AuthenticationTicketRefusedMessage());
            //Diconnected After 1s
        }
        
    }
    
    
    @HandlerAttribute(ID = ClientKeyMessage.MESSAGE_ID)
    public static void HandleClientKeyMessage(WorldClient Client, Message message) {
        Client.ClientKey = (((ClientKeyMessage) message).key);
       //Client.sendPacket(new CharacterLoadingCompleteMessage());
        
        
        
    }
    

}
