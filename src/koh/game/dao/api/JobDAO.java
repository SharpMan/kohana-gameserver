package koh.game.dao.api;

import koh.game.entities.jobs.InteractiveSkill;
import koh.game.entities.jobs.JobGatheringInfos;
import koh.patterns.services.api.Service;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class JobDAO implements Service {

    public abstract Stream<InteractiveSkill> streamSkills();

    public abstract void consumeSkills(Predicate<? super InteractiveSkill> condition, Consumer<? super InteractiveSkill> consumer);

    public abstract int findHightJob(int startLevel);

    public abstract JobGatheringInfos findGathJob(int id);
}
