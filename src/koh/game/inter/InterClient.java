package koh.game.inter;

import java.net.InetSocketAddress;
import koh.game.Main;
import koh.game.dao.DAO;
import koh.inter.InterMessage;
import koh.inter.IntercomDecoder;
import koh.inter.IntercomEncoder;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 *
 * @author Neo-Craft
 */
public class InterClient {

    private IoConnector connector = new NioSocketConnector();
    private InetSocketAddress address = new InetSocketAddress(DAO.getSettings().getStringElement("Inter.Host"), DAO.getSettings().getIntElement("Inter.Port"));
    private IoSession session;

    public InterClient() {
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new IntercomEncoder(), new IntercomDecoder()));
        connector.setHandler(new InterHandler(this));
        connector.getSessionConfig().setReadBufferSize(65536);
    }

    public void bind() {
        Main.TransfererTimeOut().addTimeOut(this);
        connector.connect(address);
    }

    public void RetryConnect(int port) {
        connector.dispose();
        connector = new NioSocketConnector();
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new IntercomEncoder(), new IntercomDecoder()));
        connector.setHandler(new InterHandler(this));
        connector.getSessionConfig().setReadBufferSize(65536);
        System.out.println("Retry to connect to the InterServer ...");
        connector.connect(address);
    }

    public void Send(InterMessage Packet) { //FIXME : add message in queue when realm offline
        try {
            this.session.write(Packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        connector.dispose(true);
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public IoSession getSession(IoSession session) {
        return session;
    }

}
