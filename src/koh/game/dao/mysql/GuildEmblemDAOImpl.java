package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.d2o.entities.EmblemSymbols;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.GuildEmblemDao;
import koh.game.utils.sql.ConnectionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Neo-Craft
 */
public class GuildEmblemDAOImpl extends GuildEmblemDao {

    private static final Logger logger = LogManager.getLogger(GuildEmblemDAOImpl.class);
    private HashMap<Integer, EmblemSymbols> symbols = new HashMap<>(50);

    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from guilds_emblems")) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                symbols.put(result.getInt("id"), new EmblemSymbols() {
                    {
                        this.idtype = result.getInt("id");
                        this.categoryIdtype = result.getInt("category_id");
                        this.iconIdtype = result.getInt("icon_id");
                        this.skinIdtype = result.getInt("skin_id");
                        this.ordertype = result.getInt("order");
                        this.colorizabletype = result.getBoolean("colorizable");

                    }
                });

                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
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
