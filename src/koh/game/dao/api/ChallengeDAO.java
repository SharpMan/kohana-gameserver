package koh.game.dao.api;

import koh.game.entities.fight.Challenge;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 9/24/16.
 */
public abstract class ChallengeDAO implements Service {
    public abstract Class<? extends Challenge> find(int id);

    public abstract int pop();
}
