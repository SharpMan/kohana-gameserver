package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DAO;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.TaxCollectorDAO;
import koh.game.entities.actors.TaxCollector;
import koh.game.utils.sql.ConnectionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
                            result.getInt("level")));
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
    public void remove(int iden) {

    }

    @Override
    public void insert(TaxCollector tax) {

    }

    @Override
    public TaxCollector find(int map) {
        return taxers.get(map);
    }

    @Override
    public void start() {
        this.taxers = new HashMap<>(300);
        logger.info("loaded {} tax collectors", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
