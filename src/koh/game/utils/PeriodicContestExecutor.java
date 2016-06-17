package koh.game.utils;

import koh.game.dao.DAO;
import koh.game.entities.actors.Player;
import koh.game.entities.kolissium.ArenaParty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Melancholia on 3/20/16.
 */
public abstract class PeriodicContestExecutor implements Runnable {

    protected final static long TIME_REFRESH = DAO.getSettings().getBoolElement("Logging.Debug") ? 3000 : 30000; //debug 3000
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);//2
    private ScheduledFuture<?> myFuture;

    protected void initialize() {
        this.myFuture = scheduler.scheduleWithFixedDelay(this, TIME_REFRESH, TIME_REFRESH, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (myFuture != null && !myFuture.isCancelled()) {
            myFuture.cancel(true);
        }
        scheduler.shutdownNow();
    }

    public abstract void registerPlayer(Player p);

    public abstract void unregisterPlayer(Player p);

    public abstract boolean unregisterGroup(Player executor, ArenaParty group);

    public abstract void unregisterGroupForced(ArenaParty group);

    public abstract boolean isRegistred(Player player);

    public abstract boolean registerGroup(Player executor, ArenaParty group);

    public void scheduleTask(Runnable r, long endCallBack) {
        scheduler.schedule(r, endCallBack, TimeUnit.MILLISECONDS);
    }

    public void executeTask(Runnable r) {
        scheduler.schedule(r, 100, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedulePeriodicTask(Runnable r, long firstCallback, long period) {
        return scheduler.scheduleWithFixedDelay(r, firstCallback, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public abstract void run();
}
