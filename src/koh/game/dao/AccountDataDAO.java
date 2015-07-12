package koh.game.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import koh.game.MySQL;
import static koh.game.MySQL.executeQuery;
import koh.game.entities.Account;
import koh.game.entities.AccountData;
import koh.game.entities.item.InventoryItem;
import koh.game.utils.Settings;
import koh.game.utils.StringUtil;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class AccountDataDAO {

    public static void Update(AccountData Data, Account Account) {
        try {
            int i = 1;
            String Query = "UPDATE `accounts_data` set ";
            Query = Data.ColumsToUpdate.stream().map((s) -> s + " =?,").reduce(Query, String::concat);
            Query = StringUtil.removeLastChar(Query);
            Query += " WHERE id = ?;";

            PreparedStatement p = MySQL.prepareQuery(Query, MySQL.Connection());

            Data.ColumsToUpdate.add("id");
            for (String s : Data.ColumsToUpdate) {
                setValue(p, s, i++, Data);
            }
            p.execute();
            MySQL.closePreparedStatement(p);

            if (Account != null) {
                Data.totalClear(Account);
            }
            Data.ColumsToUpdate.clear();
            Data.ColumsToUpdate = null;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setValue(PreparedStatement p, String Column, int Seq, AccountData Item) {
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
            e.printStackTrace();
        }
    }

    public static AccountData Find(int id) {
        AccountData toReturn = null;
        try {
            ResultSet RS = executeQuery("SELECT * FROM `accounts_data` where id = '" + id + "';", Settings.GetStringElement("Database.Name"));
            if (RS.first()) {
                toReturn = new AccountData() {
                    {
                        Id = RS.getInt("id");
                        Kamas = (int) RS.getLong("kamas");
                        Friends = AccountData.FriendContact.Deserialize(RS.getBytes("friends"));
                        Ignored = AccountData.IgnoredContact.Deserialize(RS.getBytes("ignored"));
                        Spouse = null;
                        friend_warn_on_login = RS.getBoolean("friend_warn_on_login");
                        friend_warn_on_level_gain = RS.getBoolean("friend_warn_on_level_gain");
                        guild_warn_on_login = RS.getBoolean("guild_warn_on_login");
                        ItemDAO.InitInventoryCache(Id, ItemsCache, "storage_items");
                    }
                };
            } else {
                toReturn = new AccountData() {
                    {
                        Id = id;
                        Kamas = 0;
                        Friends = new FriendContact[0];
                        Ignored = new IgnoredContact[0];
                        friend_warn_on_login = true;
                        friend_warn_on_level_gain = true;
                        guild_warn_on_login = true;
                        PreparedStatement p = MySQL.prepareQuery("INSERT INTO `accounts_data` VALUES (?,?,?,?,?,?,?,?);", MySQL.Connection());
                        p.setInt(1, Id);
                        p.setLong(2, 0);
                        p.setBytes(3, this.SerializeFriends());
                        p.setBytes(4, this.SerializeIgnored());
                        p.setBytes(5, null);
                        p.setBoolean(6, friend_warn_on_login);
                        p.setBoolean(7, friend_warn_on_level_gain);
                        p.setBoolean(8, guild_warn_on_login);
                        p.execute();
                        MySQL.closePreparedStatement(p);

                    }
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

}
