package koh.game;

import koh.game.dao.DAO;
import koh.game.inter.InterClient;
import koh.game.inter.TransfererTimeOut;
import koh.game.network.WorldServer;
import koh.game.network.handlers.Handler;
import koh.game.utils.Settings;

/**
 *
 * @author Neo-Craft
 */
public class Main {

    private volatile static Logs $Logs;
    public static int MIN_TIMEOUT = 30;
    private static boolean running;
    private volatile static TransfererTimeOut $TransfererTimeOut;
    private volatile static InterClient $InterClient;
    private volatile static WorldServer $WorldServer;

    public static WorldServer WorldServer() {
        return $WorldServer;
    }

    public static Logs Logs() {
        return $Logs;
    }
    
    public static TransfererTimeOut TransfererTimeOut() {
        return $TransfererTimeOut;
    }

    public static InterClient InterClient() {
        return $InterClient;
    }

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    close();
                }

            });
            long time = System.currentTimeMillis();
            //Settings.initialize();
            $Logs = new Logs();
            MySQL.ConnectDatabase();
            MySQL.LoadCache();
            $Logs.writeInfo(Handler.Initialize() + " HANDLERS Readed");
            $Logs.writeInfo(Handler.InitializeMessage() + " messages Readed");
            $TransfererTimeOut = new TransfererTimeOut();
            $InterClient = new InterClient();
            $InterClient.bind();
            $WorldServer = new WorldServer(DAO.getSettings().getIntElement("World.Port")).configure().launch();
            running = true;
            $Logs.writeInfo(new StringBuilder("WorldServer start in ").append(System.currentTimeMillis() - time).append(" ms.").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isRunning() {
        return running;
    }

    private static void close() {
        try {
            //$RealmServer.stop();
            MySQL.disconnectDatabase();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("[INFOS] Server shutdown success.");
        }

    }

}
