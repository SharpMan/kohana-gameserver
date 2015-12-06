package koh.game.network;

import koh.game.Main;
import koh.game.dao.DAO;
import koh.game.utils.Settings;
import koh.protocol.client.Message;
import koh.protocol.messages.game.approach.HelloGameMessage;
import koh.protocol.messages.handshake.ProtocolRequired;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Neo-Craft
 */
public class WorldHandler extends IoHandlerAdapter {

    public static byte[] rawBytes;
    public static char[] binaryKeys;

    private static final Logger logger = LogManager.getLogger(WorldHandler.class);

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        session.setAttribute("session", new WorldClient(session));
        session.write(new ProtocolRequired(DAO.getSettings().getIntElement("Protocol.requiredVersion"), DAO.getSettings().getIntElement("Protocol.currentVersion")));
        session.write(new HelloGameMessage());
    }

    /**
     *
     * @param session
     * @param arg1
     * @throws Exception
     */
    @Override
    public void messageReceived(IoSession session, Object arg1) throws Exception {
        Message message = (Message) arg1;
        logger.debug("[DEBUG] {} recv >> {}",session.getRemoteAddress(),message.getClass().getSimpleName());
        Object objClient = session.getAttribute("session");
        if (objClient != null && objClient instanceof WorldClient) {
            WorldClient client = (WorldClient) objClient;
            client.parsePacket(message);
        }
    }

    /**
     *
     * @param session
     * @param arg1
     * @throws Exception
     */
    @Override
    public void messageSent(IoSession session, Object arg1) throws Exception {
        Message message = (Message) arg1;
        logger.debug("[DEBUG] {} send >> {}",session.getRemoteAddress(),message.getClass().getSimpleName());
    }

    /**
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        Object objClient = session.getAttribute("session");
        if (objClient != null && objClient instanceof WorldClient) {
            WorldClient client = (WorldClient) objClient;
            client.timeOut();
        }
    }

    /**
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        Object objClient = session.getAttribute("session");
        if (objClient != null && objClient instanceof WorldClient) {
            WorldClient client = (WorldClient) objClient;
            client.close();
        }
        session.removeAttribute("session");

    }

    /**
     *
     * @param session
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        Object objClient = session.getAttribute("session");
        if (objClient != null && objClient instanceof WorldClient) {
            WorldClient client = (WorldClient) objClient;
            logger.error("(client->proxy->server)[ip:{}]::Error: {}",client.getIP(),ExceptionUtils.getStackTrace(cause));
            client.close();
        } else {
            logger.error("(client->proxy->server)::Error:{}" , ExceptionUtils.getStackTrace(cause));
            session.close(false);
        }
    }
}
