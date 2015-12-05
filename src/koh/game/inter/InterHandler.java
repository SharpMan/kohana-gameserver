package koh.game.inter;

import koh.game.Main;
import koh.game.dao.DAO;
import koh.game.dao.mysql.AccountTicketDAO;
import koh.game.entities.Account;
import koh.game.utils.Settings;
import koh.inter.messages.ExpulseAccountMessage;
import koh.inter.messages.HelloMessage;
import koh.inter.messages.PlayerComingMessage;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author Neo-Craft
 */
class InterHandler extends IoHandlerAdapter {

    private final InterClient connector;

    public InterHandler(InterClient aThis) {
        this.connector = aThis;
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        if (connector != null) {
            System.out.println(new StringBuilder("InterServer connected : ").append(session.getRemoteAddress().toString()));
            connector.setSession(session);
            //TODO to final var foreach 90value in dic each moment ...
            session.write(new HelloMessage(DAO.getSettings().fastElement("World.Key")));
        } else {
            session.close(false);
        }
    }

    @Override
    public void exceptionCaught(IoSession is, Throwable cause) throws Exception {
        Main.Logs().writeError("(server->proxy->client)::Error:{" + cause.getMessage() + "}::cause.toString(){" + cause.toString() + "}");
    }

    @Override
    public void messageReceived(IoSession is, Object object) throws Exception {
        if (object instanceof PlayerComingMessage) {
            AccountTicketDAO.addWaitingCompte(new Account((PlayerComingMessage) object), ((PlayerComingMessage) object).authenticationAddress, ((PlayerComingMessage) object).authenticationTicket);
        }
        if (object instanceof ExpulseAccountMessage) {
            try {
                Main.WorldServer().getClient(((ExpulseAccountMessage) object).accountId).close();
            } catch (NullPointerException e) {
            }
        }
    }

}
