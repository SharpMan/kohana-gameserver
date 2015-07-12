package koh.game.inter;

import java.util.Timer;
import java.util.TimerTask;
import koh.game.utils.Settings;

/**
 *
 * @author Alleos13
 */
public class TransfererTimeOut {

    private Timer timer;

    public TransfererTimeOut() {
        timer = new Timer("TRANSFERER_TIMEOUT");
    }

    public void addTimeOut(InterClient connector) {
        timer.schedule(new TimeOut(connector), 5000, 5000);
    }

    private class TimeOut extends TimerTask {

        private InterClient connector;

        public TimeOut(InterClient connector) {
            this.connector = connector;
        }

        @Override
        public void run() {
            try {
                if (!connector.isConnected()) {
                    connector.RetryConnect(Settings.GetIntElement("Inter.Port"));
                }
            } catch (Exception e) {
            } finally {
                //cancel();
            }
        }

        @Override
        public boolean cancel() {
            connector = null;
            try {
                return super.cancel();
            } finally {
                try {
                    this.finalize();
                } catch (Throwable e) {
                }
            }
        }
    }

    public void stop() {
        if (this.timer != null) {
            timer.cancel();
        }
    }
}
