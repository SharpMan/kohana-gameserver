package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.d2o.entities.EmblemSymbols;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.GuildEmblemDAO;
import koh.game.utils.sql.ConnectionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Neo-Craft
 */
public class GuildEmblemDAOImpl extends GuildEmblemDAO {

    private static final Logger logger = LogManager.getLogger(GuildEmblemDAO.class);
    private final HashMap<Integer, EmblemSymbols> symbols = new HashMap<>(50);

    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from guilds_emblems")) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                symbols.put(result.getInt("id"), new EmblemSymbols(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return symbols.size();
    }

    @Override
    public EmblemSymbols get(int id) {
        return this.symbols.get(id);
    }

    @Override
    public void start() {
        logger.info("Loaded {} emblem symbols", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
