package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.util.HashMap;

import com.google.inject.Inject;
import koh.game.dao.DatabaseSource;
import koh.game.dao.api.JobDAO;
import koh.game.entities.jobs.*;
import koh.game.utils.sql.ConnectionResult;
import koh.utils.Enumerable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class JobDAOImpl extends JobDAO {

    private static final Logger logger = LogManager.getLogger(JobDAO.class);

    private final HashMap<Integer, JobGatheringInfos> gatheringJobs = new HashMap<>(11);
    private final HashMap<Integer, InteractiveSkill> skills = new HashMap<>(250);


    @Inject
    private DatabaseSource dbSource;
    
    private int loadAllGathering() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from job_gathering", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                gatheringJobs.put(result.getInt("start_level"), new JobGatheringInfos() {
                    {
                        this.GatheringByLevel.put(0, this.toCouple(result.getString("level0")));
                        this.GatheringByLevel.put(20, this.toCouple(result.getString("level20")));
                        this.GatheringByLevel.put(40, this.toCouple(result.getString("level40")));
                        this.GatheringByLevel.put(60, this.toCouple(result.getString("level60")));
                        this.GatheringByLevel.put(80, this.toCouple(result.getString("level80")));
                        this.GatheringByLevel.put(100, this.toCouple(result.getString("level100")));
                        this.GatheringByLevel.put(120, this.toCouple(result.getString("level120")));
                        this.GatheringByLevel.put(140, this.toCouple(result.getString("level140")));
                        this.GatheringByLevel.put(160, this.toCouple(result.getString("level160")));
                        this.GatheringByLevel.put(180, this.toCouple(result.getString("level180")));
                        this.GatheringByLevel.put(200, this.toCouple(result.getString("level200")));
                        this.bonusMin = Integer.parseInt(result.getString("bonus").split("-")[0]);
                        this.bonusMax = Integer.parseInt(result.getString("bonus").split("-")[1]);
                        this.xpEarned = result.getInt("xp_earned");
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
    
    public int loadAllSkills() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_interactive_skills", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                skills.put(result.getInt("id"), new InteractiveSkill() {
                    {
                        ID = result.getInt("id");
                        Type = result.getString("name");
                        parentJobId = result.getByte("parent_job");
                        isForgemagus = result.getBoolean("is_forgemagus");
                        modifiableItemTypeId = Enumerable.StringToIntArray(result.getString("modifiable_item_ids"));
                        gatheredRessourceItem = result.getInt("gathered_ressource_item");
                        craftableItemIds = Enumerable.StringToIntArray(result.getString("craftable_item_ids"));
                        interactiveId = result.getInt("interactive_id");
                        useAnimation = result.getString("use_animation");
                        elementActionId = result.getInt("element_action_id");
                        isRepair = result.getBoolean("is_repair");
                        cursor = result.getInt("cursor");
                        availableInHouse = result.getBoolean("available_in_house");
                        clientDisplay = result.getBoolean("client_display");
                        levelMin = result.getInt("level_min");
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
    public void start() {
        logger.info("Loaded {} map interactive skills",this.loadAllSkills());
        logger.info("Loaded {} gathering jobs",this.loadAllGathering());
    }

    @Override
    public void stop() {

    }
}
