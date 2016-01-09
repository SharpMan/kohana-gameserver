package koh.game.dao.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import koh.game.dao.api.PetInventoryDAO;
import koh.game.entities.item.animal.PetsInventoryItemEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

public class PetInventoryDAOImpl extends PetInventoryDAO {

    private static final Logger logger = LogManager.getLogger(PetInventoryDAO.class);

    private final Dao<PetsInventoryItemEntity, Integer> dataSource;

    public PetInventoryDAOImpl() {
        try {
            this.dataSource = DaoManager.createDao(new JdbcConnectionSource("jdbc:sqlite:data/pet_inventories.db",
                    null, null), PetsInventoryItemEntity.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private volatile int nextPetId;

    @Override
    public synchronized int nextId() {
        return ++nextPetId;
    }

    private void initNextKey() {
        try {
            nextPetId = (int)dataSource.queryRawValue("select MAX(id) from pets");
        } catch (Exception e) {
            nextPetId = 0;
        }
    }

    @Override
    public void insert(PetsInventoryItemEntity entity) {
        try {
            dataSource.create(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(PetsInventoryItemEntity entity) {
        try {
            dataSource.update(entity);
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public PetsInventoryItemEntity get(int id) {
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
