package koh.game.dao.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import koh.game.dao.api.GuildDAO;
import koh.game.entities.guilds.Guild;
import koh.game.entities.guilds.GuildEntity;
import koh.protocol.types.game.guild.GuildEmblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 *
 * @author Neo-Craft
 */
public class GuildDAOImpl extends GuildDAO {

    private static final Logger logger = LogManager.getLogger(GuildDAO.class);

    private volatile int nextGuildID = 0;

    private final Dao<GuildEntity, Integer> dataSource;

    private final Map<Integer, Guild> entitiesById = new ConcurrentHashMap<>();
    private final Map<String, Guild> entitiesByName = new ConcurrentHashMap<>();

    public GuildDAOImpl() {
        try {
            this.dataSource = DaoManager.createDao(new JdbcConnectionSource("jdbc:sqlite:data/guilds.db",
                    null, null), GuildEntity.class);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(GuildEntity entity) {
        try {
            dataSource.update(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void remove(GuildEntity entity) {
        try {
            entitiesById.remove(entity.guildID);
            entitiesByName.remove(entity.name.trim().toLowerCase());
            dataSource.delete(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void insert(Guild guild) {
        try {
            dataSource.create(guild.entity);
            entitiesById.put(guild.entity.guildID, guild);
            entitiesByName.put(guild.entity.name.trim().toLowerCase(), guild);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    public synchronized int nextId() {
        return ++nextGuildID;
    }

    @Override
    public boolean alreadyTakenEmblem(GuildEmblem emblem) {
        return asStream().map(Guild::getGuildEmblem).anyMatch(currentEmblem -> currentEmblem.equals(emblem));
    }

    @Override
    public Stream<Guild> asStream() {
        return entitiesById.values().stream();
    }

    @Override
    public Guild get(int id) {
        return entitiesById.get(id);
    }

    @Override
    public Guild get(String name) {
        return entitiesByName.get(name.trim().toLowerCase());
    }

    @Override
    public Guild getForPlayer(int playerId) {
        try {
            int guildId = (int)dataSource.queryRawValue("select guild_id from guilds_members WHERE char_id = " + playerId);

            Guild found = entitiesById.get(guildId);
            return found.memberStream()
                    .anyMatch(x -> x.characterID == playerId) ? found : null;
        } catch (Exception e) {
            return null;
        }
    }

    private int loadAll() {
        try {
            dataSource.queryForAll().forEach(entity -> {
                Guild guild = new Guild(entity);
                entitiesById.put(entity.guildID, guild);
                entitiesByName.put(entity.name.trim().toLowerCase(), guild);
            });
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return 0;
        }
        return entitiesById.size();
    }

    private void initNextKey() {
        try {
            this.nextGuildID = (int)dataSource.queryRawValue("select MAX(id) from guilds");
        } catch (Exception e) {
            this.nextGuildID = 0;
        }
    }

    @Override
    public void start() {
        this.initNextKey();
        logger.info("loaded {} guilds",this.loadAll());
    }

    @Override
    public void stop() {
    }
}
