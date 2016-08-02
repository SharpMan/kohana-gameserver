package koh.game.network.websocket;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import koh.game.dao.DAO;
import koh.game.network.websocket.message.TokenMessage;
import koh.game.utils.Settings;
import koh.protocol.client.enums.CommPacketEnum;
import koh.utils.Couple;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Created by Melancholia on 2/19/16.
 */
@Log4j2
public class AutobahnServer extends WebSocketServer {

    private static int counter = 0;
    private final JSONParser parser = new JSONParser();
    private  final HashMap<CommPacketEnum, Couple<Class<? extends IMessage>,Method>> packets = new HashMap(CommPacketEnum.values().length);

    private final String[] IPS_ALLOWED;

    public AutobahnServer( int port , Draft d,Settings settings ) throws UnknownHostException {
        super( new InetSocketAddress( port ), Collections.singletonList( d ) );

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("koh.game.network.websocket.handlers"))
                .setScanners(new MethodAnnotationsScanner()));

        reflections.getMethodsAnnotatedWith(HandlerAttribute.class).forEach((method) ->
                packets.put(method.getDeclaredAnnotation(HandlerAttribute.class).ID(), new Couple<>(method.getParameterTypes()[1].asSubclass(IMessage.class),method))
        );

        this.IPS_ALLOWED = settings.getStringElement("WebSocket.Allow").split(",");
    }

    public AutobahnServer( InetSocketAddress address, Draft d,Settings settings ) {
        super( address, Collections.singletonList( d ) );
        this.IPS_ALLOWED = settings.getStringElement("WebSocket.Allow").split(",");
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        if(!ArrayUtils.contains(IPS_ALLOWED, conn.getRemoteSocketAddress().getAddress().toString().substring(1))){
            log.error("Unknow address {}", conn.getRemoteSocketAddress().toString());
            conn.close();
            return;
        }
        counter++;
        log.debug( "Opened connection number {}" , counter );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        log.debug( "WebSocket closed" );
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        log.error(ex);
        ex.printStackTrace();
    }

    @Override
    public void onMessage( WebSocket conn, String blob ) {
        try {
            if(!DAO.getItemTemplates().loaded())
                return;
            final JSONArray packet = (JSONArray) parser.parse(blob);
            final long packetId = (long) packet.get(0);
            final CommPacketEnum packetType = CommPacketEnum.valueOf(packetId);
            if(packetType == null){
                log.warn("Unknow packet {} , socket closed",packetId);
                conn.close();
            }
            else{
                log.debug("RCV -> {} with {} args", packetType, packet.size());
                try {
                    final Couple<Class<? extends IMessage>,Method> handler = this.packets.get(packetType);
                    final IMessage message = handler.first.newInstance();
                    message.deserialize(packet);
                    handler.second.invoke(null,conn,message);
                } catch (Exception e) {
                    log.error(e);
                    e.printStackTrace();
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        conn.send( blob );
    }

    @Override
    public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
        final FrameBuilder builder = (FrameBuilder) frame;
        builder.setTransferemasked( false );
        conn.sendFrame(frame);
    }


}