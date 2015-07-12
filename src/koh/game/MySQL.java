package koh.game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import koh.d2o.d2oReader;
import koh.game.dao.*;
import static koh.game.dao.PlayerDAO.AccountInUnload;
import koh.game.utils.Settings;

/**
 *
 * @author Neo-Craft
 */
public class MySQL {

    private static Connection Connection;
    private static Timer timerCommit;
    public static volatile boolean needCommit;
    private final static ReentrantLock myLocker = new ReentrantLock();
    private static boolean resettingConnection = false;

    public static Connection Connection() {
        if (myLocker.isLocked()) {
            myLocker.lock();
            try {
                try {
                    if (Connection != null && Connection.isClosed() && !resettingConnection) {
                        ResetDatabaseConnexion();
                    }
                } catch (SQLException ex) {
                }
                return Connection;
            } finally {
                myLocker.unlock();
            }
        } else {
            try {
                if (Connection != null && Connection.isClosed() && !resettingConnection) {
                    ResetDatabaseConnexion();
                }
            } catch (SQLException ex) {
            }
            return Connection;
        }
    }

    public static boolean ConnectDatabase() {
        try {
            myLocker.lock();
            try {
                Connection = DriverManager.getConnection("jdbc:mysql://" + Settings.GetStringElement("Database.Host") + "/" + Settings.GetStringElement("Database.Name"), Settings.GetStringElement("Database.User"), Settings.GetStringElement("Database.Password"));
                Connection.setAutoCommit(false);
                if ((!Connection.isValid(1000)) || (!Connection.isValid(1000))) {
                    Main.Logs().writeError("SQLError : Connexion a la BD invalide!");
                    return false;
                }
                needCommit = false;
            } finally {
                myLocker.unlock();
            }
            Main.Logs().writeInfo("MySQL Database Connected");
            TIMER(true);

            return true;
        } catch (SQLException e) {
            Main.Logs().writeError("SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static void closeResultSet(ResultSet RS) {
        try {
            RS.getStatement().close();
            RS.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closePreparedStatement(PreparedStatement p) {
        try {
            p.clearParameters();
            p.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean ResetDatabaseConnexion() {
        try {
            myLocker.lock();
            try {
                resettingConnection = true;
                TIMER(false);
                if (Connection != null) {
                    try {
                        Connection.commit();
                    } catch (Exception e) {
                    }
                    try {
                        Connection.close();
                    } catch (Exception e) {
                    }
                }
                Connection = DriverManager.getConnection("jdbc:mysql://" + Settings.GetStringElement("Database.Host") + "/" + Settings.GetStringElement("Database.Name"), Settings.GetStringElement("Database.User"), Settings.GetStringElement("Database.Password"));
                Connection.setAutoCommit(false);
                if ((!Connection.isValid(60000))) {
                    Main.Logs().writeError("SQLError : Connexion a la BD invalide!");
                    return false;
                }
                needCommit = false;
                TIMER(true);
            } finally {
                myLocker.unlock();
                resettingConnection = false;
            }
            return true;
        } catch (SQLException e) {
            Main.Logs().writeError("SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static void TIMER(boolean start) {
        if (start) {
            if (timerCommit != null) {
                timerCommit.cancel();
            }
            timerCommit = new Timer();
            timerCommit.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!MySQL.needCommit) {
                        return;
                    }

                    MySQL.commitDatabase();
                    MySQL.needCommit = false;
                }
            }, Settings.GetIntElement("Database.Commit"), Settings.GetIntElement("Database.Commit"));
        } else {
            if (timerCommit != null) {
                timerCommit.cancel();
            }
        }
    }

    public static ResultSet executeQuery(String query, String DBNAME) throws SQLException {
        Connection DB = Connection();
        Statement stat = DB.createStatement();
        ResultSet RS = stat.executeQuery(query);
        stat.setQueryTimeout(300);

        return RS;
    }

    public static ResultSet executeQuery(String query, String DBNAME, int secsTimeout) throws SQLException {
        Connection DB = Connection();
        Statement stat = DB.createStatement();
        ResultSet RS = stat.executeQuery(query);
        if (secsTimeout > 0) {
            stat.setQueryTimeout(secsTimeout);
        }
        return RS;
    }

    public static PreparedStatement prepareQuery(String baseQuery, Connection dbCon) throws SQLException {
        PreparedStatement toReturn = (PreparedStatement) dbCon.prepareStatement(baseQuery);
        needCommit = true;
        return toReturn;
    }

    public static void LoadCache() {
        D2oDao.Initialize();
        Main.Logs().writeInfo(d2oReader.Breeds.size() + " Breeds catched");
        Main.Logs().writeInfo(d2oReader.Heads.size() + " Heads catched");
        Main.Logs().writeInfo(d2oReader.Effects.size() + " Effects catched");
        PlayerDAO.InitializeNextIdentifiant();
        ItemDAO.DistinctItems();
        PetsDAO.InitNextKey();
        GuildDAO.InitNextKey();
        ItemDAO.InitializeNextIdentifiant();
        ExpDAO.load_ExpLevels();
        Main.Logs().writeInfo(AreaDAO.FindSuper() + " SuperAreas catched");
        Main.Logs().writeInfo(AreaDAO.FindAll() + " Areas catched");
        Main.Logs().writeInfo(AreaDAO.FindSubAreas() + " SubAreas catched");
        Main.Logs().writeInfo(MapDAO.FindAll() + " DofusMaps catched");
        Main.Logs().writeInfo(MapDAO.FindSatedElements() + " StatedElements catched");
        Main.Logs().writeInfo(MapDAO.FindInteractiveElements() + " InteractiveElements catched");
        Main.Logs().writeInfo(MapDAO.FindHouseInformations() + " HouseInformations catched");
        Main.Logs().writeInfo(MapDAO.FindDoors() + " InteractiveDoors catched");
        Main.Logs().writeInfo(MapDAO.FindMapPositions() + " MapPositions catched");
        Main.Logs().writeInfo(MapDAO.FindZaaps() + " MapZaaps catched");
        Main.Logs().writeInfo(MapDAO.FindSubways() + " MapSubways catched");
        Main.Logs().writeInfo(SpellDAO.FindLevels() + " SpellLevels catched ");
        Main.Logs().writeInfo(SpellDAO.FindAll() + " Spells catched ");
        Main.Logs().writeInfo(GuildEmblemDAO.FindAll() + " GuildEmblems catched ");
        Main.Logs().writeInfo(SpellDAO.FindLearnables() + " LearnableSpells catched ");
        Main.Logs().writeInfo(ItemDAO.FindAll() + " ItemTemplates catched ");
        Main.Logs().writeInfo(ItemDAO.FindWeapons() + " Weapons catched ");
        Main.Logs().writeInfo(ItemDAO.FindItemSets() + " ItemSets catched ");
        Main.Logs().writeInfo(ItemDAO.FindItemTypes() + " ItemTypes catched ");
        Main.Logs().writeInfo(ItemDAO.FindPets() + " ItemPets catched ");
        Main.Logs().writeInfo(NpcDAO.FindAll() + " NpcTemplates catched ");
        Main.Logs().writeInfo(NpcDAO.FindSpawns() + " NpcSpawns catched ");
        Main.Logs().writeInfo(NpcDAO.FindItems() + " NpcItems catched ");
        Main.Logs().writeInfo(NpcDAO.FindMessages() + " NpcMessages catched ");
        Main.Logs().writeInfo(NpcDAO.FindReplies() + " NpcReplies catched ");
        Main.Logs().writeInfo(MapDAO.FindTriggers() + " Triggers catched");
        Main.Logs().writeInfo(PaddockDAO.FindAll() + " Paddocks catched");
        Main.Logs().writeInfo(MountDAO.FindAll() + " Mounts catched");
        Main.Logs().writeInfo(GuildDAO.FindAll() + " Guild catched");
        Main.Logs().writeInfo(GuildDAO.FindMembers() + " GuildMembers catched");
        Main.Logs().writeInfo(JobDAO.FindAllGatheringInfos() + " GatheringJob LevelInfos catched");
        Main.Logs().writeInfo(JobDAO.FindAllSkills() + " InteractiveSkills LevelInfos catched");
    }

    public synchronized Savepoint commitDatabase(boolean createSavePoint) {
        if (createSavePoint) {
            try {
                myLocker.lock();
                try {
                    if (Connection != null && !Connection.isClosed()) {
                        Savepoint point = Connection.setSavepoint();
                        Connection.commit();
                        return point;
                    }
                } finally {
                    myLocker.unlock();
                }
            } catch (SQLException e) {
                Main.Logs().writeError("SQL ERROR:" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            commitDatabase();
        }
        return null;
    }

    public static synchronized void commitDatabase() {
        try {
            Connection dbCon = Connection();
            dbCon.commit();
            synchronized (AccountInUnload) {
                AccountInUnload.clear();
            }
        } catch (SQLException ex) {
            Main.Logs().writeError("SQL ERROR:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static synchronized void rollbackDatabase(Savepoint point) {
        try {
            myLocker.lock();
            try {
                if (Connection != null && !Connection.isClosed()) {
                    Connection.rollback(point);
                }
            } finally {
                myLocker.unlock();
            }
        } catch (SQLException ex) {
            Main.Logs().writeError("SQL ERROR:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static synchronized void disconnectDatabase() {
        try {
            myLocker.lock();
            try {
                if (Connection != null && !Connection.isClosed()) {
                    Connection.commit();
                    Connection.close();
                }
            } finally {
                myLocker.unlock();
            }
        } catch (Exception e) {
            Main.Logs().writeError("Erreur a la fermeture des connexions SQL:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
