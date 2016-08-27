package koh.game;

import koh.concurrency.CancellableScheduledRunnable;
import koh.game.app.AppModule;
import koh.game.app.Loggers;
import koh.game.app.MemoryService;
import koh.game.app.WebSocketService;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.entities.actors.Player;
import koh.game.entities.guilds.GuildMember;
import koh.game.inter.InterClient;
import koh.game.inter.TransfererTimeOut;
import koh.game.network.WorldServer;
import koh.game.network.handlers.Handler;
import koh.patterns.services.ServicesProvider;
import koh.protocol.client.enums.TextInformationTypeEnum;
import koh.protocol.messages.game.basic.TextInformationMessage;
import koh.protocol.messages.game.moderation.PopupWarningMessage;
import koh.protocol.messages.server.basic.SystemMessageDisplayMessage;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Neo-Craft
 */
public class Main {

    public static final int MIN_TIMEOUT = 15;
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

    private static final ScheduledExecutorService SAVE_WORKER = Executors.newSingleThreadScheduledExecutor();

    private static ScheduledExecutorService REBOOT_WORKER;

    private static CancellableScheduledRunnable saveRunnable;

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            public void print(final String string) {
                realPrintStream.print(string);
                logger.error(string);
            }
        };
    }

    public static int DAY_OF_WEEK = 0;


    public static void main(String[] args) {
        try {
            final long time = System.currentTimeMillis();
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
            scheduleReboot(DAO.getSettings().getIntElement("World.REBOOT"));

            saveRunnable = new CancellableScheduledRunnable(SAVE_WORKER, 60 * 1000 * 60,60 * 1000 * 60) {

                @Override
                public void run() {
                    try{
                        DAY_OF_WEEK=  Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris")).get(Calendar.DAY_OF_WEEK);
                        if(DAY_OF_WEEK == Calendar.FRIDAY){
                            if( WorldServer.getKoli().getTEAM_SIZE() != 2) {
                                WorldServer.getKoli().setTEAM_SIZE(2);
                                WorldServer.getKoli().setPOOL_SIZE(4);
                            }
                        }
                        else if(WorldServer.getKoli().getTEAM_SIZE() == 2){
                            WorldServer.getKoli().setTEAM_SIZE(DAO.getSettings().getIntElement("Koliseo.Size"));
                            WorldServer.getKoli().setPOOL_SIZE(DAO.getSettings().getIntElement("Koliseo.Size") *2);
                        }

                        logger.info("Save starting...");
                        worldServer.sendPacket(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR,164));
                        DAO.getPlayers().getPlayers().forEach(pl -> pl.save(false));
                        DAO.getGuilds().asStream().forEach(Guild ->
                            Guild.memberStream().forEach(GuildMember::save)
                        );
                        DAO.getGuilds().getEntites().forEach(DAO.getGuilds()::update);
                        worldServer.sendPacket(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR, 165));

                    }
                    catch (Exception e2){
                        e2.printStackTrace();
                    }
                    finally {
                        logger.info("Save Ended");
                    }
                }
            };

        } catch (Exception e) {
            logger.fatal(e);
            logger.error(e.getMessage());
        }
    }

    private static void close() {
        try {
            SAVE_WORKER.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("Server shutdown success.");
        }

    }

    private static int minuteLeft = 0;

    public static void scheduleReboot(int hour){
        if(REBOOT_WORKER != null){
            REBOOT_WORKER.shutdownNow();
        }
        if(hour == 0){
            return;
        }
        REBOOT_WORKER  = Executors.newSingleThreadScheduledExecutor();
        REBOOT_WORKER.scheduleWithFixedDelay(() -> {
            REBOOT_WORKER.scheduleWithFixedDelay(() -> {
                if (minuteLeft == 10) {
                    System.exit(0);
                    return;
                }
                if (minuteLeft == 5) {
                    worldServer.sendPacket(new PopupWarningMessage((byte) 5, "Melancholia","Le serveur va redemarré dans 5minutes (Durée d'indisponibilité 13s)"));
                }else{
                    worldServer.sendPacket(new TextInformationMessage(TextInformationTypeEnum.TEXT_INFORMATION_ERROR,15, ((10 - minuteLeft) + " " + (minuteLeft == 9 ? "minute" : "minutes"))));
                }
                minuteLeft++;
            },1,1, TimeUnit.MINUTES);
        },hour,hour, TimeUnit.HOURS);
    };

}
