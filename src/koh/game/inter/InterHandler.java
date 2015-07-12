package koh.game.inter;

import koh.game.Main;
import koh.game.dao.AccountTicketDAO;
import koh.game.entities.Account;
import koh.game.network.WorldServer;
import koh.game.utils.Settings;
import koh.inter.messages.ExpulseAccountMessage;
import koh.inter.messages.HelloMessage;
import koh.inter.messages.PlayerCommingMessage;
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
            session.write(new HelloMessage(Settings.FastElement("World.Key")));
        } else {
            session.close(false);
        }
    }

    @Override
    public void exceptionCaught(IoSession is, Throwable cause) throws Exception {
        Main.Logs().writeError("(server->proxy->client)::Error:{" + cause.getMessage() + "}::cause.toString(){" + cause.toString() + "}");
    }

    @Override
    public void messageReceived(IoSession is, Object o) throws Exception {
        if (o instanceof PlayerCommingMessage) {
            AccountTicketDAO.addWaitingCompte(new Account() {
                {
                    ID = ((PlayerCommingMessage) o).AccountID;
                    NickName = ((PlayerCommingMessage) o).Nickname;
                    Right = ((PlayerCommingMessage) o).Right;
                    SecretQuestion = ((PlayerCommingMessage) o).SecretQuestion;
                    SecretAnswer = ((PlayerCommingMessage) o).SecretAnswer;
                    LastIP = ((PlayerCommingMessage) o).LastIP;
                    last_login = ((PlayerCommingMessage) o).last_login;
                }
            }, ((PlayerCommingMessage) o).CurrentIP, ((PlayerCommingMessage) o).Ticket);
        }
        if (o instanceof ExpulseAccountMessage) {
            try {
                Main.WorldServer().getClient(((ExpulseAccountMessage) o).ID).close();
            } catch (NullPointerException e) {
            }
        }
    }

}
