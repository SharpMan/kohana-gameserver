package koh.game.dao.script;

import koh.game.dao.api.ArenaBattleDAO;
import koh.game.entities.kolissium.ArenaBattle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 6/9/16.
 */
public class ArenaBattleDAOImpl extends ArenaBattleDAO {

    private Map<Integer, ArenaBattle> battles;


    @Override
    public ArenaBattle find(int id){
        return this.battles.get(id);
    }

    @Override
    public void add(ArenaBattle arena){
        this.battles.put(arena.getId(), arena);
    }

    @Override
    public void remove(int id){
        this.battles.remove(id);
    }

    @Override
    public void start() {
        this.battles = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void stop() {

    }
}
