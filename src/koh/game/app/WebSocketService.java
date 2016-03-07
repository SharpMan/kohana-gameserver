package koh.game.app;

import com.google.inject.Inject;
import koh.game.network.websocket.AutobahnServer;
import koh.game.utils.Settings;
import koh.patterns.services.api.DependsOn;
import koh.patterns.services.api.Service;
import lombok.extern.log4j.Log4j2;
import org.java_websocket.drafts.Draft_17;

import java.io.IOException;

/**
 * Created by Melancholia on 2/19/16.
 */
@Log4j2
@DependsOn(Loggers.class)
public class WebSocketService implements Service {

    AutobahnServer server;


    @Inject
    private Settings settings;

    @Override
    public void start() {
        try {
            this.server = new AutobahnServer(settings.getIntElement("WebSocket.Port"), new Draft_17(),settings );
            this.server.start();
            log.info("WebSocket server stated on port {}",settings.getIntElement("WebSocket.Port"));
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
