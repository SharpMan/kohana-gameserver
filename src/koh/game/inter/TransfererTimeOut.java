package koh.game.inter;

import koh.inter.messages.PingMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Melancholia
 */
public class TransfererTimeOut {


    private final ScheduledExecutorService executor;
    private final InterClient client;
    private final PingMessage message;

    public TransfererTimeOut(InterClient client) {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.client = client;
        this.message = new PingMessage();

        this.executor.scheduleWithFixedDelay((Runnable) () -> {
            if (!client.isConnected()) {
                client.retryConnect();
            }
        }, 7, 7, TimeUnit.SECONDS);

        this.executor.scheduleWithFixedDelay((Runnable) () -> {
            client.send(this.message);
        }, 5, 5, TimeUnit.MINUTES);

    }

    public void stop() {
        this.executor.shutdownNow();
    }
}
