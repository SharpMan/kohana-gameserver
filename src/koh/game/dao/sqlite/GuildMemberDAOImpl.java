package koh.game.dao.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import koh.game.dao.DAO;
import koh.game.dao.api.GuildMemberDAO;
import koh.game.entities.guilds.GuildMember;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class GuildMemberDAOImpl extends GuildMemberDAO {

    private static final Logger logger = LogManager.getLogger(GuildMemberDAO.class);

    private final Dao<GuildMember, Integer> dataSource;

    public GuildMemberDAOImpl() {
        try {
            this.dataSource = DaoManager.createDao(new JdbcConnectionSource("jdbc:sqlite:data/guild_members.db",
                    null, null), GuildMember.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GuildMember get(int playerId) {
        try {
            int guildId = (int)dataSource.queryRawValue("select guild_id from guilds_members WHERE char_id = " + playerId);
            return DAO.getGuilds().get(guildId).memberStream()
                    .filter(x -> x.characterID == playerId).findFirst().get();
        } catch (Exception e) {
            return null;
        }
    }

    private int loadAll() {
        try {
            dataSource.queryForAll().forEach(x -> {
                {
                    try {
                        DAO.getGuilds().get(x.guildID).addMember(x);
                    } catch (NullPointerException e) {
                        logger.warn("{} nulled for guildmember {}", x.guildID, x.characterID);
                    }
                }
            });
            return (int)dataSource.countOf();
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
            return 0;
        }
    }

    @Override
    public void insert(GuildMember entity) {
        try {
            dataSource.deleteById(entity.characterID);
            dataSource.create(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(GuildMember member) {
        try {
            dataSource.update(member);
        } catch (SQLException e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void delete(GuildMember entity) {
        try {
            dataSource.delete(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void start() {
        logger.info("{} guild members loaded", this.loadAll());
    }

    @Override
    public void stop() {

    }
}
