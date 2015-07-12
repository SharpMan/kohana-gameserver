package koh.game.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import koh.game.entities.guild.GuildEntity;
import koh.game.entities.guild.GuildMember;
import koh.game.entities.item.animal.MountInventoryItemEntity;
import koh.game.entities.item.animal.PetsInventoryItem;
import koh.game.entities.item.animal.PetsInventoryItemEntity;

/**
 *
 * @author Neo-Craft
 */
public class PetsDAO {

    public static JdbcConnectionSource connectionSource = null;
    protected static boolean isConnectionExpected = false;
    protected static DatabaseType databaseType = null;

    protected static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:extern.sqlite";
    protected static String databaseHost = null;
    protected static String databaseUrl = DEFAULT_DATABASE_URL;
    protected static String userName = null;
    protected static String password = null;

    private static Dao<PetsInventoryItemEntity, Integer> accountDao;
    private static Dao<MountInventoryItemEntity, Integer> mountsDao;

    public static void doOpenConnectionSource() throws Exception {
        if (connectionSource == null) {
            isConnectionExpected = isConnectionExpected();
            if (isConnectionExpected) {
                connectionSource = new JdbcConnectionSource(databaseUrl, userName, password);
                accountDao = DaoManager.createDao(connectionSource, PetsInventoryItemEntity.class);
                mountsDao = DaoManager.createDao(connectionSource, MountInventoryItemEntity.class);
                GuildDAO.guildsDao = DaoManager.createDao(connectionSource, GuildEntity.class);
                GuildDAO.guildsMembersDao = DaoManager.createDao(connectionSource, GuildMember.class);
            }
        }
        if (databaseType == null) {
            if (connectionSource != null) {
                databaseType = connectionSource.getDatabaseType();
            }
        } else {
            if (connectionSource != null) {
                connectionSource.setDatabaseType(databaseType);
            }
        }
    }

    protected static boolean isConnectionExpected() throws IOException {
        try {
            if (databaseHost == null) {
                return true;
            } else {
                return InetAddress.getByName(databaseHost).isReachable(500);
            }
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public static void InitNextKey() {
        try {
            doOpenConnectionSource();
            GenericRawResults<String[]> rawResults = accountDao.queryRaw("select MAX(id) from pets");
            List<String[]> results = rawResults.getResults();
            String[] resultArray = results.get(0);
            if (resultArray == null || resultArray[0] == null) {
                ItemDAO.NextPetsID = 0;
            } else {
                ItemDAO.NextPetsID = Integer.parseInt(resultArray[0]) + 1;
            }
            rawResults = accountDao.queryRaw("select MAX(id) from mounts");
            results = rawResults.getResults();
            resultArray = results.get(0);
            if (resultArray == null || resultArray[0] == null) {
                ItemDAO.NextMountsID = 0;
            } else {
                ItemDAO.NextMountsID = Integer.parseInt(resultArray[0]) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Insert(PetsInventoryItemEntity Item) {
        try {
            doOpenConnectionSource();
            accountDao.create(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Update(PetsInventoryItemEntity Item) {
        try {
            doOpenConnectionSource();
            accountDao.update(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PetsInventoryItemEntity Get(int id) {
        try {
            doOpenConnectionSource();
            return accountDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void Insert(MountInventoryItemEntity Item) {
        try {
            doOpenConnectionSource();
            mountsDao.create(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void Update(MountInventoryItemEntity Item) {
        try {
            doOpenConnectionSource();
            mountsDao.update(Item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MountInventoryItemEntity GetMount(int id) {
        try {
            doOpenConnectionSource();
            return mountsDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
