package koh.game.dao.api;

import koh.d2o.entities.Breed;
import koh.d2o.entities.Effect;
import koh.d2o.entities.Head;
import koh.d2o.entities.SpellState;
import koh.patterns.services.api.Service;
import koh.protocol.client.enums.FightStateEnum;

/**
 * Created by Melancholia on 11/28/15.
 */
public abstract class D2oDAO implements Service {

    public abstract SpellState getState(FightStateEnum state);

    public abstract SpellState getState(int state);

    public abstract Effect getEffect(int effectId);

    public abstract Head getHead(int head);

    public abstract Breed getBreed(int breedId);



}
