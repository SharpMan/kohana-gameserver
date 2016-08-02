package koh.game.network.handlers.game.approach;

import koh.game.dao.mysql.AccountTicketDAO;
import koh.game.network.WorldClient;
import koh.game.network.WorldServer;
import koh.game.network.handlers.HandlerAttribute;
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
    public static void AuthenticationTicketMessage(WorldClient client, AuthenticationTicketMessage message) {
        if(client.isHasSentTicket()){
            client.forceClose();
            return;
        }
        client.setHasSentTicket(true);
        client.setTempTicket(AccountTicketDAO.getWaitingCompte(message.ticket));

        if (client.getTempTicket() != null && client.getTempTicket().isCorrect(client.getIP(), message.ticket)) {
            System.out.println("isCorrect");
            WorldServer.gameLoader.addClient(client);
            if (WorldServer.gameLoader.getPosition(client) != 1) {
                client.send(new LoginQueueStatusMessage((short) WorldServer.gameLoader.getPosition(client), (short) WorldServer.gameLoader.getTotal()));
                client.setShowQueue(true);
            }
        } else {
            client.send(new AuthenticationTicketRefusedMessage());
            //Diconnected After 1s
        }
        
    }
    
    
    @HandlerAttribute(ID = ClientKeyMessage.MESSAGE_ID)
    public static void HandleClientKeyMessage(WorldClient Client, ClientKeyMessage message) {
        Client.setClientKey(message.key);
       //client.sendPacket(new CharacterLoadingCompleteMessage());
        
        
        
    }
    

}
