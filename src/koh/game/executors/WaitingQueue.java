package koh.game.executors;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import koh.game.actions.GameActionTypeEnum;
import koh.game.dao.DAO;
import koh.game.fights.FightTypeEnum;
import koh.game.network.WorldClient;
import koh.protocol.messages.connection.LoginQueueStatusMessage;

/**
 *
 * @author Neo-Craft
 */
public class WaitingQueue implements Runnable {

    private final WorldClient clientHolding;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    private static final LoginQueueStatusMessage pregenWaitingMessage = new LoginQueueStatusMessage((short) 1, (short) 50);
    private static final LoginQueueStatusMessage pregenDoneMessage = new LoginQueueStatusMessage((short) 0, (short) 0);
    private final ScheduledFuture<?> future;



    public WaitingQueue(WorldClient Client) {
        this.clientHolding = Client;
        this.future = executor.scheduleWithFixedDelay(this, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if(DAO.getPlayers().isLocked()){
            if (DAO.getPlayers().isCurrentlyOnProcess(this.clientHolding.getAccount().id)
                    || this.clientHolding.getAccount().characters.parallelStream().anyMatch(Player -> Player.getFighter() != null && Player.getFight() != null && Player.getFight().getFightType() == FightTypeEnum.FIGHT_TYPE_CHALLENGE)) {
                clientHolding.send(pregenWaitingMessage);
                return;
            }
        }

        this.clientHolding.send(pregenDoneMessage);
        this.clientHolding.endGameAction(GameActionTypeEnum.WAITING);
        this.clientHolding.threatWaiting();
        try {
            this.future.cancel(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void abort(){
        try {
            this.future.cancel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
