package koh.game.dao.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import com.google.inject.Inject;
import koh.game.MySQL;
import static koh.game.MySQL.executeQuery;
import static koh.game.entities.item.InventoryItem.DeserializeEffects;

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
        int i = 1;
        String query = "UPDATE `accounts_data` set ";
        query = data.ColumsToUpdate.stream().map((s) -> s + " =?,").reduce(query, String::concat);
        query = StringUtil.removeLastChar(query);
        query += " WHERE id = ?;";

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement(query)) {
            PreparedStatement pStatement = conn.getStatement();

            data.ColumsToUpdate.add("id");
            for (String columnName : data.ColumsToUpdate) {
                setValue(pStatement, columnName, i++, data);
            }

            pStatement.execute();
            data.ColumsToUpdate.clear();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private static void setValue(PreparedStatement p, String Column, int Seq, AccountData Item) {
        try {
            switch (Column) {
                case "id":
                    p.setInt(Seq, Item.Id);
                    break;
                case "kamas":
                    p.setLong(Seq, Item.Kamas);
                    break;
                case "friends":
                    p.setBytes(Seq, Item.SerializeFriends());
                    break;
                case "ignored":
                    p.setBytes(Seq, Item.SerializeIgnored());
                    break;
                case "spouse":
                    p.setBytes(Seq, null);
                    break;
                case "friend_warn_on_login":
                    p.setBoolean(Seq, Item.friend_warn_on_login);
                    break;
                case "friend_warn_on_level_gain":
                    p.setBoolean(Seq, Item.friend_warn_on_level_gain);
                    break;
                case "guild_warn_on_login":
                    p.setBoolean(Seq, Item.guild_warn_on_login);
                    break;

            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private static final String SAVE_ACCOUNT_DATA = "INSERT INTO `accounts_data` VALUES (?,?,?,?,?,?,?,?);";

    @Override
    public AccountData get(int id) {

        try (ConnectionResult conn = dbSource.executeQuery("SELECT * FROM `accounts_data` where id = '" + id + "';")) {
            ResultSet result = conn.getResult();

            if(result.first()) {
                return new AccountData() {
                    {
                        Id = result.getInt("id");
                        Kamas = (int) result.getLong("kamas");
                        Friends = AccountData.FriendContact.Deserialize(result.getBytes("friends"));
                        Ignored = AccountData.IgnoredContact.Deserialize(result.getBytes("ignored"));
                        Spouse = null;
                        friend_warn_on_login = result.getBoolean("friend_warn_on_login");
                        friend_warn_on_level_gain = result.getBoolean("friend_warn_on_level_gain");
                        guild_warn_on_login = result.getBoolean("guild_warn_on_login");

                        try (Statement statement = conn.getConnection().createStatement()) {
                            ResultSet result = statement.executeQuery("SELECT * from storage_items where owner =" + Id + ";");
                            while (result.next()) {
                                List<ObjectEffect> effects = DeserializeEffects(result.getBytes("effects"));
                                ItemsCache.put(result.getInt("id"), InventoryItem.Instance(
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
                        Id = id;
                        Kamas = 0;
                        Friends = new FriendContact[0];
                        Ignored = new IgnoredContact[0];
                        friend_warn_on_login = true;
                        friend_warn_on_level_gain = true;
                        guild_warn_on_login = true;

                        try(PreparedStatement pStatement = conn.getConnection().prepareStatement(SAVE_ACCOUNT_DATA)) {
                            pStatement.setInt(1, Id);
                            pStatement.setLong(2, 0);
                            pStatement.setBytes(3, this.SerializeFriends());
                            pStatement.setBytes(4, this.SerializeIgnored());
                            pStatement.setBytes(5, null);
                            pStatement.setBoolean(6, friend_warn_on_login);
                            pStatement.setBoolean(7, friend_warn_on_level_gain);
                            pStatement.setBoolean(8, guild_warn_on_login);
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
