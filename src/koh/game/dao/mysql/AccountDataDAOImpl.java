package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.google.inject.Inject;

import static koh.game.entities.item.InventoryItem.deserializeEffects;

import koh.game.dao.DatabaseSource;
import koh.game.dao.api.AccountDataDAO;
import koh.game.entities.Account;
import koh.game.entities.AccountData;
import koh.game.entities.item.InventoryItem;
import koh.game.utils.StringUtil;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.data.items.ObjectEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class AccountDataDAOImpl extends AccountDataDAO {

    private static final Logger logger = LogManager.getLogger(AccountDataDAO.class);

    @Inject
    private DatabaseSource dbSource;

    @Override
    public void save(AccountData data, Account account) {
        //TODO extract them to Entity-Side (they pollute 1feature-per-class logic...)
        if(data.columsToUpdate.isEmpty())
            return;
        int i = 1;
        String query = "UPDATE `accounts_data` set ";
        query = data.columsToUpdate.stream().map((s) -> "`"+s + "` =?,").reduce(query, String::concat);
        query = StringUtil.removeLastChar(query);
        query += " WHERE id = ?;";

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement(query)) {
            PreparedStatement pStatement = conn.getStatement();

            data.columsToUpdate.add("id");
            for (String columnName : data.columsToUpdate) {
                setValue(pStatement, columnName, i++, data);
            }

            pStatement.execute();
            data.columsToUpdate.clear();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private static void setValue(PreparedStatement p, String Column, int seq, AccountData Item) {
        try {
            switch (Column) {
                case "id":
                    p.setInt(seq, Item.id);
                    break;
                case "right":
                    p.setByte(seq, Item.right);
                    break;
                case "kamas":
                    p.setLong(seq, Item.kamas);
                    break;
                case "friends":
                    p.setBytes(seq, Item.serializeFriends());
                    break;
                case "ignored":
                    p.setBytes(seq, Item.serializeIgnored());
                    break;
                case "spouse":
                    p.setBytes(seq, null);
                    break;
                case "friend_warn_on_login":
                    p.setBoolean(seq, Item.friend_warn_on_login);
                    break;
                case "friend_warn_on_level_gain":
                    p.setBoolean(seq, Item.friend_warn_on_level_gain);
                    break;
                case "guild_warn_on_login":
                    p.setBoolean(seq, Item.guild_warn_on_login);
                    break;

            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private static final String SAVE_ACCOUNT_DATA = "INSERT INTO `accounts_data` VALUES (?,?,?,?,?,?,?,?,?);";

    @Override
    public AccountData get(int accountId) {

        try (ConnectionResult conn = dbSource.executeQuery("SELECT * FROM `accounts_data` where id = '" + accountId + "';")) {
            ResultSet result = conn.getResult();

            if(result.first()) {
                return new AccountData() {
                    {
                        id = result.getInt("id");
                        kamas = (int) result.getLong("kamas");
                        friends = AccountData.FriendContact.deserialize(result.getBytes("friends"));
                        ignored = AccountData.IgnoredContact.deserialize(result.getBytes("ignored"));
                        spouse = null;
                        friend_warn_on_login = result.getBoolean("friend_warn_on_login");
                        friend_warn_on_level_gain = result.getBoolean("friend_warn_on_level_gain");
                        guild_warn_on_login = result.getBoolean("guild_warn_on_login");
                        right = result.getByte("right");

                        try (Statement statement = conn.getConnection().createStatement()) {
                            ResultSet result = statement.executeQuery("SELECT * from storage_items where owner =" + id + ";");
                            while (result.next()) {
                                List<ObjectEffect> effects = deserializeEffects(result.getBytes("effects"));
                                itemscache.put(result.getInt("id"), InventoryItem.getInstance(
                                        result.getInt("id"),
                                        result.getInt("template"),
                                        result.getInt("position"),
                                        result.getInt("owner"),
                                        result.getInt("stack"),
                                        effects
                                ));
                            }
                        } catch (Exception e) {
                            logger.error(e);
                            logger.warn(e.getMessage());
                        }
                    }
                };
            } else {
                return new AccountData() {
                    {
                        id = accountId;
                        kamas = 0;
                        friends = new FriendContact[0];
                        ignored = new IgnoredContact[0];
                        friend_warn_on_login = true;
                        friend_warn_on_level_gain = true;
                        guild_warn_on_login = true;

                        try(PreparedStatement pStatement = conn.getConnection().prepareStatement(SAVE_ACCOUNT_DATA)) {
                            pStatement.setInt(1, id);
                            pStatement.setLong(2, 0);
                            pStatement.setBytes(3, this.serializeFriends());
                            pStatement.setBytes(4, this.serializeIgnored());
                            pStatement.setBytes(5, null);
                            pStatement.setBoolean(6, friend_warn_on_login);
                            pStatement.setBoolean(7, friend_warn_on_level_gain);
                            pStatement.setBoolean(8, guild_warn_on_login);
                            pStatement.setInt(9,0);
                            pStatement.execute();
                        }
                    }
                };
            }
        } catch(Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
