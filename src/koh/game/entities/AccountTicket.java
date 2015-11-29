package koh.game.entities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import koh.game.dao.mysql.AccountTicketDAO;

/**
 *
 * @author Alleos13
 */
public class AccountTicket {

    private String key;
    private Account account;
    private String ip;
    private Timer timer;

    public String getKey() {
        return key;
    }

    public String getIP() {
        return ip;
    }

    public AccountTicket(Account account, String ip,String ticket) {
        this.account = account;
        this.ip = ip;
        this.key = ticket;
        //timer = createTimer();
        //timer.start();
    }

    private Timer createTimer() {
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                clear();
            }
        };

        return new Timer(5000, action);
    }

    public void clear() {
        AccountTicketDAO.delWaitingCompte(this);
        this.key = null;
        this.account = null;
        //Todo Unload after 15min
        this.ip = null;
        if (timer != null) {
            timer.stop();
        }
        this.timer = null;
    }

    public Account valid() {
        try {
            return account;
        } finally {
            //clear();
        }
    }

    public boolean isValid() {
       // return timer != null && timer.isRunning();
        return true;
    }

    public boolean isCorrect(String GT_ip, String infos) {
        if (!isValid()) {
            return false;
        }
        /*if (infos.length != 2) {
            return false;
        }*/
        if (key.equals(infos) /*&& ip.equals(infos[1])*/ && ip.equals(GT_ip)) {
            return true;
        }
        return false;
    }
}
