package koh.game.dao.mysql;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    @Override
    public Stream<InteractiveSkill> streamSkills(){
        return this.skills.values().stream();
    }

    @Override
    public void consumeSkills(Predicate<? super InteractiveSkill> condition, Consumer<? super InteractiveSkill> consumer){ //TODO: not found the right name
        this.skills.values().stream().filter(condition).forEach(consumer);
    }

    @Override
    public int findHightJob(int startLevel){
        return this.gatheringJobs.keySet().stream().filter(x -> startLevel >= x).mapToInt(x -> x).max().getAsInt();
    }

    @Override
    public JobGatheringInfos findGathJob(int id){
        return this.gatheringJobs.get(id);
    }
    
    private int loadAllGathering() {
        int i = 0;
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from job_gathering", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                gatheringJobs.put(result.getInt("start_level"), new JobGatheringInfos() {
                    {
                        for(int i=0; i<=200; i+=20) {
                            this.gatheringByLevel.put(i, this.toCouple(result.getString("level"+i)));
                        }
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
                        type = result.getString("name");
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
