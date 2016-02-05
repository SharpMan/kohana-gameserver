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

import static koh.game.entities.item.InventoryItem.deserializeEffects;

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
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private void initNextId() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT max(id) FROM `character_items`;")) {
            ResultSet result = conn.getResult();
            if (!result.first())
                nextId = 0;
            else
                nextId = result.getInt(1) +1;

            try(Statement statement = conn.getConnection().createStatement()) {
                ResultSet storageResult = statement.executeQuery("SELECT max(id) FROM `storage_items`;");

                if (!storageResult.first())
                    nextStorageId = 0;
                else
                    nextStorageId = storageResult.getInt(1) +1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void initInventoryCache(int player, Map<Integer, InventoryItem> cache, String table) {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from " + table + " where owner =" + player + ";")) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                List<ObjectEffect> effects = deserializeEffects(result.getBytes("effects"));
                cache.put(result.getInt("id"), InventoryItem.getInstance(
                        result.getInt("id"),
                        result.getInt("template"),
                        result.getInt("position"),
                        result.getInt("owner"),
                        result.getInt("stack"),
                        effects
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean create(InventoryItem item, boolean clear, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `" + table + "` VALUES (?,?,?,?,?,?);")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.getID());
            pStatement.setInt(2, item.getOwner());
            pStatement.setInt(3, item.getTemplateId());
            pStatement.setInt(4, item.getPosition());
            pStatement.setInt(5, item.getQuantity());
            pStatement.setBytes(6, item.serializeEffectInstanceDice().array());

            item.setNeedInsert(false);
            item.columsToUpdate = null;

            pStatement.execute();

            //TODO better dispose/totalClear pattern
            return true;
        }
        catch(com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException e1){
            try (ConnectionResult conn = dbSource.executeQuery("SELECT template,owner FROM `character_items` WHERE id = "+item.getID()+";")) {
                ResultSet result = conn.getResult();
                if(result.first()){
                    logger.error("Duplicate {} item oldTemplate,owner {} {} ,new {} {}",item.getID(),result.getInt("template"),result.getInt("owner"),item.getTemplateId(),item.getOwner());
                }
                e1.printStackTrace();
            }
            catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean save(InventoryItem item, boolean clear, String table) {
        int i = 1;
        String query = "UPDATE `" + table + "` set ";
        query = item.columsToUpdate.stream().map((s) -> s + " =?,").reduce(query, String::concat);
        query = StringUtil.removeLastChar(query);
        query += " WHERE id = ?;";

        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement(query)) {
            PreparedStatement pStatement = conn.getStatement();

            item.columsToUpdate.add("id");
            for (String columnName : item.columsToUpdate) {
                setValue(pStatement, columnName, i++, item);
            }

            item.columsToUpdate.clear();
            item.columsToUpdate = null;

            pStatement.execute();

            //TODO better dispose/totalClear pattern
            /*if (clear) {
                item.totalClear();
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
                    p.setInt(Seq, Item.getID());
                    break;
                case "owner":
                    p.setInt(Seq, Item.getOwner());
                    break;
                case "stack":
                    p.setInt(Seq, Item.getQuantity());
                    break;
                case "position":
                    p.setInt(Seq, Item.getPosition());
                    break;
                case "effects":
                    p.setBytes(Seq, Item.serializeEffectInstanceDice().array());
                    //p.setBlob(Seq, new SerialBlob(item.serializeEffectInstanceDice()));
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean delete(InventoryItem item, String table) {
        try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `" + table + "` WHERE id = ?;")) {
            PreparedStatement pStatement = conn.getStatement();

            pStatement.setInt(1, item.getID());
            pStatement.execute();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
