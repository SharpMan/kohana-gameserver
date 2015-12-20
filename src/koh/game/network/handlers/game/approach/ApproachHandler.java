package koh.game.network.handlers.game.approach;

import koh.game.dao.mysql.AccountTicketDAO;
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
        Client.send(new StartupActionsListMessage(new StartupActionAddObject[0]));//client.send(Message);
    }

    @HandlerAttribute(ID = AuthenticationTicketMessage.MESSAGE_ID)
    public static void AuthenticationTicketMessage(WorldClient Client, Message message) {

        Client.setTempTicket(AccountTicketDAO.getWaitingCompte(((AuthenticationTicketMessage) message).Ticket));

        if (Client.getTempTicket() != null && Client.getTempTicket().isCorrect(Client.getIP(), ((AuthenticationTicketMessage) message).Ticket)) {
            WorldServer.Loader.addClient(Client);
            if (WorldServer.Loader.getPosition(Client) != 1) {
                Client.send(new LoginQueueStatusMessage((short) WorldServer.Loader.getPosition(Client), (short) WorldServer.Loader.getTotal()));
                Client.setShowQueue(true);
            }
        } else {
            Client.send(new AuthenticationTicketRefusedMessage());
            //Diconnected After 1s
        }
        
    }
    
    
    @HandlerAttribute(ID = ClientKeyMessage.MESSAGE_ID)
    public static void HandleClientKeyMessage(WorldClient Client, ClientKeyMessage message) {
        Client.setClientKey(message.key);
       //client.sendPacket(new CharacterLoadingCompleteMessage());
        
        
        
    }
    

}
