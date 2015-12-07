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
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from job_gathering", 0)) {
            ResultSet result = conn.getResult();
            while (result.next()) {
                gatheringJobs.put(result.getInt("start_level"), new JobGatheringInfos(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return gatheringJobs.size();
    }
    
    public int loadAllSkills() {
        try (ConnectionResult conn = dbSource.executeQuery("SELECT * from maps_interactive_skills", 0)) {
            ResultSet result = conn.getResult();

            while (result.next()) {
                skills.put(result.getInt("id"), new InteractiveSkill(result));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.warn(e.getMessage());
        }
        return skills.size();
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
