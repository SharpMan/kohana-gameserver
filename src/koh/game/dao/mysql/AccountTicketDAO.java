package koh.game.dao.mysql;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.entities.Account;
import koh.game.entities.AccountTicket;

/**
 *
 * @author Neo-Craft
 */
public class AccountTicketDAO {

    private static Map<Integer, AccountTicket> accountGuid = Collections.synchronizedMap(new HashMap<Integer, AccountTicket>());
    public static Map<String, AccountTicket> _waitings = Collections.synchronizedMap(new HashMap<String, AccountTicket>());

    public static AccountTicket getWaitingCompte(String key) {
        synchronized (_waitings) {
            if (_waitings.containsKey(key)) {
                return _waitings.get(key);
            }
        }
        return null;
    }

    public static void delWaitingCompte(AccountTicket ticket) {
        synchronized (_waitings) {
            if (accountGuid.containsKey(ticket.valid().id)) {
                accountGuid.remove(ticket.valid().id);
            }
            if (_waitings.containsKey(ticket.getKey())) {
                _waitings.remove(ticket.getKey());
            }
        }
    }

    public static AccountTicket addWaitingCompte(Account _compte, String ip, String ticket) {

        if (accountGuid.containsKey(_compte.id)) {
            try {
                // ActualCharacted accountGuid.get(_compte.get_GUID()).valid().getGameThread().closeSocket(false);
            } catch (Exception e) {
            } finally {
                accountGuid.remove(_compte.id);
            }
            return null;
        }
        synchronized (_waitings) {
            AccountTicket t = new AccountTicket(_compte, ip, ticket);
            accountGuid.put(_compte.id, t);
            _waitings.put(ticket, t);
            return t;
        }
    }

}
