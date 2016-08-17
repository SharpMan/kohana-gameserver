package koh.game.dao.mysql;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.MonsterDAO;
import koh.game.entities.mob.MonsterDrop;
import koh.game.entities.mob.MonsterGrade;
import koh.game.entities.mob.MonsterTemplate;
import koh.game.utils.sql.ConnectionResult;
import koh.game.utils.sql.ConnectionStatement;
import koh.utils.SimpleLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * @author Neo-Craft
 */
public class MonsterDAOImpl extends MonsterDAO {

    private static final Logger logger = LogManager.getLogger(MonsterDAOImpl.class);

    @Inject
    private DatabaseSource dbSource;


    @Override
    public MonsterTemplate find(int id){
        return this.templates.get(id);
    }

    @Override
    public void update(MonsterGrade gr, String column, int value) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `monster_grades` SET `"+column+"` = ? WHERE `monster_id` = ? AND `grade` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, value);
                pStatement.setByte(3,gr.getGrade());
                pStatement.setInt(2, gr.getMonsterId());
                pStatement.executeUpdate();

                try {
                    SimpleLogger koliseoLog = new SimpleLogger("logs/koli/" + SimpleLogger.getCurrentDayStamp() + ".txt", 0);
                    koliseoLog.write(pStatement.toString());
                    koliseoLog.newLine();
                    koliseoLog.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try (ConnectionStatement<PreparedStatement> connn = dbSource.prepareStatement("UPDATE `monster_grades` SET `"+column+"` = ? WHERE `monster_id` = ? AND `"+column+"` = 0;")) {
                    PreparedStatement pStatement2 = connn.getStatement();
                    pStatement2.setInt(1, value);
                    pStatement2.setInt(2, gr.getMonsterId());
                    pStatement2.executeUpdate();



                } catch (Exception e) {
                    logger.error(e);
                    logger.warn(e.getMessage());
                }

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    @Override
    public void update(MonsterGrade gr) {
        try {
            try (ConnectionStatement<PreparedStatement> conn = dbSource.prepareStatement("UPDATE `monster_grades` SET `strength` = ?,`chance` = ?,`intelligence` = ?,`agility` = ? WHERE `monster_id` = ? AND `grade` = ?;")) {
                PreparedStatement pStatement = conn.getStatement();
                pStatement.setInt(1, gr.getStrenght());
                pStatement.setInt(2, gr.getChance());
                pStatement.setInt(3, gr.getIntelligence());
                pStatement.setInt(4, gr.getAgility());
                pStatement.setByte(6,gr.getGrade());
                pStatement.setInt(5, gr.getMonsterId());
                pStatement.executeUpdate();

                try (ConnectionStatement<PreparedStatement> connn = dbSource.prepareStatement("UPDATE `monster_grades` SET `strength` = ?,`chance` = ?,`intelligence` = ?,`agility` = ? WHERE `monster_id` = ? AND `strength` = 0 AND `chance` = 0 AND `intelligence` = 0 AND `agility` = 0;")) {
                    PreparedStatement pStatement2 = connn.getStatement();
                    pStatement2.setInt(1, gr.getStrenght());
                    pStatement2.setInt(2, gr.getChance());
                    pStatement2.setInt(3, gr.getIntelligence());
                    pStatement2.setInt(4, gr.getAgility());
                    pStatement2.setInt(5, gr.getMonsterId());
                    pStatement2.executeUpdate();

                } catch (Exception e) {
                    logger.error(e);
                    logger.warn(e.getMessage());
                }

            } catch (Exception e) {
                logger.error(e);
                logger.warn(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
    }

    private int loadAll() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_templates", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.put(result.getInt("id"), new MonsterTemplate(result));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return templates.size();
    }

    private int loadAllDrops() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_drops", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                 templates.get(result.getInt("monster_id")).getDrops().add(new MonsterDrop(result));
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    private int loadAllGrades() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from monster_grades ORDER by grade ASC", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                templates.get(result.getInt("monster_id")).getGrades().add(new MonsterGrade(result));
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return i;
    }

    @Override
    public void start() {
        logger.info("Loaded {} template monster", this.loadAll());
        logger.info("Loaded {} template grades", this.loadAllGrades());
        logger.info("Loaded {} template drops", this.loadAllDrops());
    }

    @Override
    public void stop() {

    }
}
