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

//import static koh.game.dao.mysql.PlayerDAOImpl.AccountInUnload;

import koh.game.dao.DAO;

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
                Connection = DriverManager.getConnection("jdbc:mysql://" + DAO.getSettings().getStringElement("Database.Host") + "/" + DAO.getSettings().getStringElement("Database.name"), DAO.getSettings().getStringElement("Database.User"), DAO.getSettings().getStringElement("Database.Password"));
                Connection.setAutoCommit(false);
                if ((!Connection.isValid(1000)) || (!Connection.isValid(1000))) {
                   // Main.Logs().writeError("SQLError : Connexion a la BD invalide!");
                    return false;
                }
                needCommit = false;
            } finally {
                myLocker.unlock();
            }
            //Main.Logs().writeInfo("MySQL Database Connected");
            TIMER(true);

            return true;
        } catch (SQLException e) {
            //Main.Logs().writeError("SQL ERROR: " + e.getMessage());
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
                Connection = DriverManager.getConnection("jdbc:mysql://" + DAO.getSettings().getStringElement("Database.Host") + "/" + DAO.getSettings().getStringElement("Database.name"), DAO.getSettings().getStringElement("Database.User"), DAO.getSettings().getStringElement("Database.Password"));
                Connection.setAutoCommit(false);
                if ((!Connection.isValid(60000))) {
                    //Main.Logs().writeError("SQLError : Connexion a la BD invalide!");
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
            //Main.Logs().writeError("SQL ERROR: " + e.getMessage());
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
            }, DAO.getSettings().getIntElement("Database.Commit"), DAO.getSettings().getIntElement("Database.Commit"));
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
        /*D2oDaoImpl.loadAll();
        Main.Logs().writeInfo(d2oReader.Breeds.size() + " Breeds catched");
        Main.Logs().writeInfo(d2oReader.Heads.size() + " Heads catched");
        Main.Logs().writeInfo(d2oReader.Effects.size() + " effects catched");
        PlayerDAOImpl.InitializeNextIdentifiant();
        ItemTemplateDAOImpl.DistinctItems();
        PetsDAO.InitNextKey();
        GuildDAO.InitNextKey();
        ItemTemplateDAOImpl.InitializeNextIdentifiant();
        ExpDAOImpl.load_ExpLevels();
        Main.Logs().writeInfo(AreaDAOImpl.FindSuper() + " SuperAreas catched");
        Main.Logs().writeInfo(AreaDAOImpl.FindAll() + " areas catched");
        Main.Logs().writeInfo(AreaDAOImpl.FindSubAreas() + " subAreas catched");
        Main.Logs().writeInfo(MapDAOImpl.FindAll() + " DofusMaps catched");
        Main.Logs().writeInfo(MapDAOImpl.loadAllSatedElements() + " StatedElements catched");
        Main.Logs().writeInfo(MapDAOImpl.FindInteractiveElements() + " interactiveElements catched");
        Main.Logs().writeInfo(MapDAOImpl.loadALlHouseInformations() + " HouseInformations catched");
        Main.Logs().writeInfo(MapDAOImpl.loadAllDoors() + " InteractiveDoors catched");
        Main.Logs().writeInfo(MapDAOImpl.loadALlPositions() + " MapPositions catched");
        Main.Logs().writeInfo(MapDAOImpl.FindZaaps() + " MapZaaps catched");
        Main.Logs().writeInfo(MapDAOImpl.FindSubways() + " MapSubways catched");
        Main.Logs().writeInfo(SpellDAOImpl.FindLevels() + " SpellLevels catched ");
        Main.Logs().writeInfo(SpellDAOImpl.FindAll() + " spells catched ");
        Main.Logs().writeInfo(SpellDAOImpl.loadAllBombs() + " SpellBombs catched ");
        Main.Logs().writeInfo(GuildEmblemDAOImpl.FindAll() + " GuildEmblems catched ");
        Main.Logs().writeInfo(SpellDAOImpl.loadLearnables() + " learnableSpells catched ");
         Main.Logs().writeInfo(ItemTemplateDAOImpl.FindItemTypes() + " ItemTypes catched ");
        Main.Logs().writeInfo(ItemTemplateDAOImpl.FindAll() + " ItemTemplates catched ");
        Main.Logs().writeInfo(ItemTemplateDAOImpl.FindWeapons() + " Weapons catched ");
        Main.Logs().writeInfo(ItemTemplateDAOImpl.FindItemSets() + " ItemSets catched ");
        Main.Logs().writeInfo(ItemTemplateDAOImpl.FindPets() + " ItemPets catched ");
        Main.Logs().writeInfo(NpcDAOImpl.loadAll() + " NpcTemplates catched ");
        Main.Logs().writeInfo(NpcDAOImpl.FindSpawns() + " NpcSpawns catched ");
        Main.Logs().writeInfo(NpcDAOImpl.FindItems() + " NpcItems catched ");
        Main.Logs().writeInfo(NpcDAOImpl.FindMessages() + " NpcMessages catched ");
        Main.Logs().writeInfo(NpcDAOImpl.FindReplies() + " NpcReplies catched ");
        Main.Logs().writeInfo(MapDAOImpl.FindTriggers() + " Triggers catched");
        Main.Logs().writeInfo(PaddockDAOImpl.FindAll() + " Paddocks catched");
        Main.Logs().writeInfo(MountDAOImpl.FindAll() + " Mounts catched");
        Main.Logs().writeInfo(GuildDAO.FindAll() + " guild catched");
        Main.Logs().writeInfo(GuildDAO.FindMembers() + " guildMembers catched");
        Main.Logs().writeInfo(JobDAOImpl.loadAllGatheringInfos() + " GatheringJob LevelInfos catched");
        Main.Logs().writeInfo(JobDAOImpl.loadAllSkills() + " InteractiveSkills LevelInfos catched");
        Main.Logs().writeInfo(MonsterDAOImpl.FindAll() + " Monster Templates catched");
        Main.Logs().writeInfo(MonsterDAOImpl.FindGrades() + " getMonster Grades catched");
        Main.Logs().writeInfo(MonsterDAOImpl.FindDrops() + " getMonster Drops catched");*/
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
                //Main.Logs().writeError("SQL ERROR:" + e.getMessage());
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
            /*synchronized (AccountInUnload) {
                AccountInUnload.clear();
            }*/
        } catch (SQLException ex) {
            //Main.Logs().writeError("SQL ERROR:" + ex.getMessage());
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
            //Main.Logs().writeError("SQL ERROR:" + ex.getMessage());
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
            //Main.Logs().writeError("Erreur a la fermeture des connexions SQL:" + e.getMessage());
            e.printStackTrace();
        }
    }

}
