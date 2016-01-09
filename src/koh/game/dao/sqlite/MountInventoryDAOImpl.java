package koh.game.dao.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import koh.game.dao.api.MountInventoryDAO;
import koh.game.entities.item.animal.MountInventoryItemEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class MountInventoryDAOImpl extends MountInventoryDAO {

    private static final Logger logger = LogManager.getLogger(MountInventoryDAO.class);

    private final Dao<MountInventoryItemEntity, Integer> dataSource;

    public MountInventoryDAOImpl() {
        try {
            this.dataSource = DaoManager.createDao(new JdbcConnectionSource("jdbc:sqlite:data/mount_inventories.db",
                    null, null), MountInventoryItemEntity.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private volatile int nextMountId;

    @Override
    public synchronized int nextId() {
        return ++nextMountId;
    }

    private void initNextKey() {
        try {
            nextMountId = (int)dataSource.queryRawValue("select MAX(id) from mounts");
        } catch (Exception e) {
            nextMountId = 0;
        }
    }

    @Override
    public void insert(MountInventoryItemEntity entity) {
        try {
            dataSource.create(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(MountInventoryItemEntity entity) {
        try {
            dataSource.update(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public MountInventoryItemEntity get(int id) {
        try {
            return dataSource.queryForId(id);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return null;
    }

    @Override
    public void start() {
        this.initNextKey();
    }

    @Override
    public void stop() {

    }
}
