package koh.game;

import koh.game.app.AppModule;
import koh.game.app.Loggers;
import koh.game.app.MemoryService;
import koh.game.app.WebSocketService;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.inter.InterClient;
import koh.game.inter.TransfererTimeOut;
import koh.game.network.WorldServer;
import koh.game.network.handlers.Handler;
import koh.patterns.services.ServicesProvider;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;

/**
 * @author Neo-Craft
 */
public class Main {

    public static final int MIN_TIMEOUT = 30;
    private static final Logger logger = LogManager.getLogger(Main.class);
    @Getter
    private volatile static TransfererTimeOut transfererTimeOut;
    private volatile static InterClient interClient;
    private volatile static WorldServer worldServer;

    public static WorldServer getWorldServer() {
        return worldServer;
    }

    public static InterClient getInterClient() {
        return interClient;
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                realPrintStream.print(string);
                logger.error(string);
            }
        };
    }

    public static void main(String[] args) {
        try {
            long time = System.currentTimeMillis();
            System.setErr(createLoggingProxy(System.err));
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    close();
                }

            });


            AppModule app = new AppModule();
            ServicesProvider services = app.create(
                    new DatabaseSource(),
                    new WebSocketService(),
                    new MemoryService(),
                    new Loggers()
            );

            services.start(app.resolver());


            logger.info("{} messageHandlers loaded", Handler.initialize());
            logger.info("{} messages loaded", Handler.initializeMessage());
            interClient = new InterClient().bind();
            transfererTimeOut = new TransfererTimeOut(interClient);
            worldServer = new WorldServer(DAO.getSettings().getIntElement("World.Port")).configure().launch();
            logger.info("WorldServer start in {} ms.", (System.currentTimeMillis() - time));

        } catch (Exception e) {
            logger.fatal(e);
            logger.error(e.getMessage());
        }
    }

    private static void close() {
        try {
            //$RealmServer.stop();
            //MySQL.disconnectDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("Server shutdown success.");
        }

    }

}
