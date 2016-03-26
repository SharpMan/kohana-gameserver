package koh.game.dao.mysql;

import java.sql.ResultSet;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MountDAO;
import koh.game.entities.item.animal.MountTemplate;
import java.util.HashMap;

import koh.game.utils.sql.ConnectionResult;
import koh.look.EntityLookParser;
import koh.protocol.types.game.data.items.effects.ObjectEffectInteger;
import koh.utils.Couple;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class MountDAOImpl extends MountDAO {

    private HashMap<Integer, MountTemplate> cache = new HashMap<>();

    private static final Logger logger = LogManager.getLogger(MountDAOImpl.class);
    @Inject
    private DatabaseSource dbSource;

    private int loadAll() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from mount_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                cache.put(result.getInt("scroll_id"), new MountTemplate(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return cache.size();
    }

    @Override
    public MountTemplate findTemplate(int model) {
        return cache.get(model);
    }

    @Override
    public MountTemplate find(int model) {
        return cache.values().stream().filter(x -> x.getId() == model).findFirst().orElse(null);
    }

    @Override
    public ObjectEffectInteger[] getMountByEffect(int model, int level) {
        ObjectEffectInteger[] Effects = new ObjectEffectInteger[0];
        for (Couple<Integer, Double> Stat : find(model).getStats()) {
            if ((int) (level / Stat.second) <= 0) {
                continue;
            }
            Effects = ArrayUtils.add(Effects, new ObjectEffectInteger(Stat.first, (int) (level / Stat.second)));
        }
        return Effects;
    }

    @Override
    public void start() {
        logger.info("Loaded {} mount templates",this.loadAll());
    }

    @Override
    public void stop() {

    }
}
