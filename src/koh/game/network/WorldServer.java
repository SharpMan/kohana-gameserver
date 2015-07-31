package koh.game.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import koh.game.Main;
import koh.game.executors.GameLoader;
import koh.game.network.codec.ProtocolDecoder;
import koh.game.utils.Settings;
import koh.protocol.client.Message;
import koh.protocol.client.codec.ProtocolEncoder;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author Neo-Craft
 */
public class WorldServer {

    private final NioSocketAcceptor acceptor;
    private final InetSocketAddress adress;
    public static GameLoader Loader = new GameLoader();

    public WorldServer(int port) {
        this.acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() * 4);
        this.adress = new InetSocketAddress(Settings.GetStringElement("World.Host"), port);
    }

    public WorldServer configure() {
        acceptor.setReuseAddress(true);
        acceptor.setBacklog(100000);

        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ProtocolEncoder(), new ProtocolDecoder()));
        this.acceptor.setHandler(new WorldHandler());

        //this.acceptor.getSessionConfig().setMaxReadBufferSize(2048); 
        //this.acceptor.getSessionConfig().setReadBufferSize(1024); // Debug
        this.acceptor.getSessionConfig().setReaderIdleTime(Main.MIN_TIMEOUT * 60);
        this.acceptor.getSessionConfig().setTcpNoDelay(true);
        this.acceptor.getSessionConfig().setKeepAlive(true);

        return this;
    }

    public WorldServer launch() {
        try {
            //Connect the acceptor with the HostAdress
            this.acceptor.bind(adress);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return this;
        //this.inactivity_manager.start();
    }

    public void SendPacket(Message message) {
        acceptor.getManagedSessions().values().stream().filter((session) -> (session.getAttribute("session") instanceof WorldClient) /*&& ((RealmClient) session.getAttribute("session")).ClientState == State.ON_GAMESERVER_LIST*/).forEach((session) -> {
            ((WorldClient) session.getAttribute("session")).Send(message);
        });
    }

    public ArrayList<WorldClient> getAllClient() {
        ArrayList<WorldClient> client = new ArrayList<>();
        acceptor.getManagedSessions().values().stream().filter((session) -> (session.getAttribute("session") instanceof WorldClient)).forEach((session) -> {
            client.add((WorldClient) session.getAttribute("session"));
        });
        return client;
    }

    public WorldClient getClient(int guid) {
        for (IoSession session : acceptor.getManagedSessions().values()) {
            if (session.getAttribute("session") instanceof WorldClient) {
                WorldClient client = (WorldClient) session.getAttribute("session");
                if (client.getAccount() != null && client.getAccount().ID == guid) {
                    return client;
                }
            }
        }
        return null;
    }

    public void stop() throws InterruptedException {
        acceptor.unbind();
        acceptor.dispose(true);
    }

}
