package koh.game.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import koh.game.network.WorldClient;

/**
 *
 * @author Alleos13
 */
public class GameLoader implements Runnable {

    private final List<WorldClient> waitingList;
    private final ScheduledExecutorService executor;

    public GameLoader() {
        executor = Executors.newSingleThreadScheduledExecutor();
        this.waitingList = new ArrayList<WorldClient>();
        executor.scheduleWithFixedDelay(this, 1500, 1500, TimeUnit.MILLISECONDS);
    }

    public void addClient(WorldClient t) {
        synchronized (waitingList) {
            waitingList.add(t);
            waitingList.notify();
        }
    }

    @Override
    public void run() {
        WorldClient toThreat = null;
        synchronized (waitingList) {
            while (waitingList.isEmpty()) {
                try {
                    waitingList.wait();
                } catch (InterruptedException ex) {
                }
            }
            toThreat = waitingList.remove(0);
        }
        try {
            if (toThreat != null) {
                toThreat.threatWaiting();
            }
        } catch (Exception e) {
        }
    }

    public int getPosition(WorldClient t) {
        return waitingList.indexOf(t) + 1;
    }

    public int getTotal() {
        return waitingList.size();
    }

    public void onClientDisconnect(WorldClient t) {
        synchronized (waitingList) {
            if (waitingList.contains(t)) {
                waitingList.remove(t);
            }
        }
    }
}
