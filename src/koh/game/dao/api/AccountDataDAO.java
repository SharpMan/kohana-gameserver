package koh.game.dao.api;

import koh.game.entities.Account;
import koh.game.entities.AccountData;
import koh.patterns.services.api.Service;

public abstract class AccountDataDAO implements Service {

    public abstract void save(AccountData data, Account account);

    public abstract AccountData get(int id);

}
