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

    public static AccountTicket addWaitingCompte(Account compte, String ip, String ticket) {

        if (accountGuid.containsKey(compte.id)) {
            try {
                accountGuid.get(compte.id).valid().currentCharacter.getClient().close();
            } catch (Exception e) {
            } finally {
                accountGuid.remove(compte.id);
            }
            return null; //TODO ticket null....
        }
        synchronized (_waitings) {
            final AccountTicket t = new AccountTicket(compte, ip, ticket);
            accountGuid.put(compte.id, t);
            _waitings.put(ticket, t);
            return t;
        }
    }

}
