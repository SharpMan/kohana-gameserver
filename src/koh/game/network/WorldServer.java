package koh.game.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import koh.game.Main;
import koh.game.actions.GameKolissium;
import koh.game.dao.DAO;
import koh.game.entities.kolissium.KolizeumExecutor;
import koh.game.executors.GameLoader;
import koh.game.network.codec.ProtocolDecoder;
import koh.protocol.client.Message;
import koh.protocol.client.codec.Dofus2ProtocolEncoder;
import lombok.Getter;
import lombok.Setter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

/**
 *
 * @author Neo-Craft
 */
public class WorldServer {

    private final NioSocketAcceptor acceptor;
    private final InetSocketAddress address;
    public static GameLoader gameLoader = new GameLoader();
    @Getter @Setter
    private static KolizeumExecutor koli = new KolizeumExecutor();

    /**
     * 2 * estimated client optimal size (64)
     */
    private static final int DEFAULT_READ_SIZE = /*1024512*/ 256;

    /**
     * max used client packet size + additional size for infos of the next packet
     */
    private static final int MAX_READ_SIZE = /*4096 + 0xFF*/ 2048 + 0xFF;

    public WorldServer(int port) {
        this.acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() * 4);
        this.address = new InetSocketAddress(DAO.getSettings().getStringElement("World.Host"), port);
    }

    public WorldServer configure() {
        acceptor.setReuseAddress(true);
        acceptor.setBacklog(100000);

        this.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new Dofus2ProtocolEncoder(),
                new ProtocolDecoder()));
        this.acceptor.setHandler(new WorldHandler());

        this.acceptor.getSessionConfig().setMaxReadBufferSize(MAX_READ_SIZE);
        //this.acceptor.getSessionConfig().setReceiveBufferSize(MAX_READ_SIZE);
        this.acceptor.getSessionConfig().setMinReadBufferSize(DEFAULT_READ_SIZE);
        this.acceptor.getSessionConfig().setReaderIdleTime(Main.MIN_TIMEOUT * 60);
        this.acceptor.getSessionConfig().setTcpNoDelay(false);
        this.acceptor.getSessionConfig().setKeepAlive(true);

        return this;
    }

    public WorldServer launch() {
        try {
            //Connect the acceptor with the HostAdress
            this.acceptor.bind(address);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return this;
        //this.inactivity_manager._oStart();
    }

    public void sendPacket(Message message) {
        acceptor.getManagedSessions().values().stream().filter((session) -> (session.getAttribute("session") instanceof WorldClient) /*&& ((RealmClient) session.getAttribute("session")).ClientState == State.ON_GAMESERVER_LIST*/).forEach((session) -> {
            ((WorldClient) session.getAttribute("session")).send(message);
        });
    }

    public ArrayList<WorldClient> getAllClient() {
        ArrayList<WorldClient> client = new ArrayList<>();
        acceptor.getManagedSessions().values().stream().filter((session) -> (session.getAttribute("session") instanceof WorldClient)).forEach((session) -> {
            client.add((WorldClient) session.getAttribute("session"));
        });
        return client;
    }

    public int size(){
        return this.acceptor.getManagedSessionCount();
    }

    public WorldClient getClient(int guid) {
        for (IoSession session : acceptor.getManagedSessions().values()) {
            if (session.getAttribute("session") instanceof WorldClient) {
                final WorldClient client = (WorldClient) session.getAttribute("session");
                if (client.getAccount() != null && client.getAccount().id == guid) {
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
