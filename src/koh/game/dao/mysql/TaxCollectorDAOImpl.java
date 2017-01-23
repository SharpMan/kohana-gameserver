package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.TaxCollectorDAO;
import koh.game.entities.actors.TaxCollector;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 12/4/16.
 */
public class TaxCollectorDAOImpl extends TaxCollectorDAO {

    private final Logger logger = LogManager.getLogger(TaxCollectorDAO.class);
    private Map<Integer, TaxCollector> taxers;

    @Inject
    private DatabaseSource dbSource;


    @Override
    public int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from tax_collectors", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                try {
                    taxers.put(result.getInt("map"), new TaxCollector(
                            result.getShort("cell"),
                            DAO.getGuilds().get(result.getInt("guild")),
                            result.getInt("first_name"),
                            result.getInt("last_name"),
                            result.getInt("map"),
                            result.getInt("id"),
                            result.getLong("experience"),
                            result.getInt("kamas"),
                            result.getInt("attacks_count"),
                            result.getString("caller_name"),
                            result.getInt("honor"),
                            result.getString("gathered_item")
                    ));
                }
                catch (Exception e){
                    e.printStackTrace();
                    logger.error("TaxCollector {}", result.getInt("id"));
                }

                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void update(TaxCollector tax){
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `tax_collectors` SET `honor` = ?,`experience` = ?,`kamas` = ?,`gathered_item` = ?  WHERE `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, tax.getHonor());
                pStatement.setLong(2, tax.getExperience());
                pStatement.setInt(3, tax.getKamas());
                pStatement.setString(4, Enumerable.join2(tax.getGatheredItem()));
                pStatement.setInt(5, tax.getIden());
                pStatement.executeUpdate();

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void updateSummmary(TaxCollector tax){
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `tax_collectors` SET `attacks_count` = ?  WHERE `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, tax.getAttacksCount());
                pStatement.setInt(2, tax.getIden());
                pStatement.executeUpdate();

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void remove(int iden, int map) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `tax_collectors` WHERE `id` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, iden);
                pStatement.execute();
                this.taxers.remove(map);

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void removeGuild(int guild) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("DELETE from `tax_collectors` WHERE `guild` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, guild);
                pStatement.execute();

            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public boolean insert(TaxCollector tax) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("INSERT INTO `tax_collectors` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);",true)) {

                PreparedStatement pStatement = conn.getStatement();

                pStatement.setInt(1, 0);
                pStatement.setByte(2, tax.getDirection());
                pStatement.setInt(3, tax.getMapid());
                pStatement.setShort(4, tax.getCellID()); //10
                pStatement.setInt(5, tax.getGuild().getEntity().guildID);
                pStatement.setInt(6, tax.getFirstName());
                pStatement.setInt(7, tax.getLastName());
                pStatement.setLong(8, tax.getExperience());
                pStatement.setInt(9, tax.getKamas());
                pStatement.setInt(10, tax.getAttacksCount());
                pStatement.setString(11, tax.getCallerName());
                pStatement.setInt(12, tax.getHonor());
                pStatement.setString(13, "");
                pStatement.execute();
                ResultSet resultSet = pStatement.getGeneratedKeys();
                if (!resultSet.first())//character not created ?
                    return false;
                tax.setIden(resultSet.getInt(1));
                this.taxers.put(tax.getMapid(),tax);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        finally {
            return true;
        }
    }

    @Override
    public TaxCollector find(int map) {
        return taxers.get(map);
    }

    @Override
    public boolean isPresentOn(int map) { return taxers.containsKey(map);}

    @Override
    public void start() {
        this.taxers = new HashMap<>(300);
        logger.info("loaded {} tax collectors", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
