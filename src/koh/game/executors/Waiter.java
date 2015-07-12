package koh.game.executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import koh.game.dao.PlayerDAO;
import koh.game.network.WorldClient;
import koh.protocol.messages.connection.LoginQueueStatusMessage;

/**
 *
 * @author Neo-Craft
 */
public class Waiter implements Runnable {

    private WorldClient waitingList;
    private final ScheduledExecutorService executor;

    public Waiter(WorldClient Client) {
        executor = Executors.newSingleThreadScheduledExecutor();
        this.waitingList = Client;
        executor.scheduleWithFixedDelay(this, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (PlayerDAO.AccountInUnload.contains(this.waitingList.getAccount().ID)) {
            waitingList.Send(new LoginQueueStatusMessage((short) 1, (short) 50)); 
            //Wait to comit....
            return;
        }
        waitingList.Send(new LoginQueueStatusMessage((short) 0, (short) 0));
        waitingList.threatWaiting();
        try {
            waitingList = null;
            executor.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
