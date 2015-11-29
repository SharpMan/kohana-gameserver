package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.ItemDAO;
import koh.game.entities.item.InventoryItem;
import koh.game.utils.StringUtil;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.protocol.types.game.data.items.ObjectEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static koh.game.entities.item.InventoryItem.DeserializeEffects;

public class ItemDAOImpl extends ItemDAO {

    private static final Logger logger = LogManager.getLogger(ItemDAO.class);

    @Inject
    private DatabaseSource dbSource;

    private volatile int nextId;
    private volatile int nextStorageId;

    @Override
    public synchronized int nextItemId() {
        return ++nextId;
    }

    @Override
    public synchronized int nextItemStorageId() {
        return ++nextStorageId;
    }

    private void distinctItems() {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `character_items` WHERE owner = ?;")) {
            PreparedStatement pStatement = conn.getStatement();
            pStatement.setInt(1, -1);
            pStatement.execute();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private void initNextId() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT id FROM `character_items` ORDER BY id DESC LIMIT 1;")) {
            ResultSet result = conn.getResult();
            if (!result.first())
                nextId = 0;
            else
                nextId = result.getInt("id");
            ++nextId;

            try(Statement statement = conn.getConnection().createStatement()) {
                ResultSet storageResult = statement.executeQuery("SELECT id FROM `storage_items` ORDER BY id DESC LIMIT 1;");

                if (!storageResult.first())
                    nextStorageId = 0;
                else
                    nextStorageId = storageResult.getInt("id");
                ++nextStorageId;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void initInventoryCache(int player, Map<Integer, InventoryItem> cache, String table) {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from " + table + " where owner =" + player + ";")) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                List<ObjectEffect> effects = DeserializeEffects(result.getBytes("effects"));
                cache.put(result.getInt("id"), InventoryItem.Instance(
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

    @Override
    public boolean create(InventoryItem item, boolean clear, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `" + table + "` VALUES (?,?,?,?,?,?);")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.ID);
            pStatement.setInt(2, item.GetOwner());
            pStatement.setInt(3, item.TemplateId);
            pStatement.setInt(4, item.GetPosition());
            pStatement.setInt(5, item.GetQuantity());
            pStatement.setBytes(6, item.SerializeEffectInstanceDice().array());

            item.NeedInsert = false;
            item.ColumsToUpdate = null;

            pStatement.execute();

            //TODO better Dispose/totalClear pattern
            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean save(InventoryItem item, boolean clear, String table) {
        int i = 1;
        String query = "UPDATE `" + table + "` set ";
        query = item.ColumsToUpdate.stream().map((s) -> s + " =?,").reduce(query, String::concat);
        query = StringUtil.removeLastChar(query);
        query += " WHERE id = ?;";

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement(query)) {
            PreparedStatement pStatement = conn.getStatement();

            item.ColumsToUpdate.add("id");
            for (String columnName : item.ColumsToUpdate) {
                setValue(pStatement, columnName, i++, item);
            }

            item.ColumsToUpdate.clear();
            item.ColumsToUpdate = null;

            pStatement.execute();

            //TODO better Dispose/totalClear pattern
            /*if (Clear) {
                Item.totalClear();
            }*/

            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    private static void setValue(PreparedStatement p, String Column, int Seq, InventoryItem Item) {
        try {
            switch (Column) {
                case "id":
                    p.setInt(Seq, Item.ID);
                    break;
                case "owner":
                    p.setInt(Seq, Item.GetOwner());
                    break;
                case "stack":
                    p.setInt(Seq, Item.GetQuantity());
                    break;
                case "position":
                    p.setInt(Seq, Item.GetPosition());
                    break;
                case "effects":
                    p.setBytes(Seq, Item.SerializeEffectInstanceDice().array());
                    //p.setBlob(Seq, new SerialBlob(Item.SerializeEffectInstanceDice()));
                    break;

            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean delete(InventoryItem item, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `" + table + "` WHERE id = ?;")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.ID);
            pStatement.execute();

            return true;
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }

        return false;
    }

    @Override
    public void start() {
        this.distinctItems();
        this.initNextId();
    }

    @Override
    public void stop() {

    }
}
