package koh.game.dao.api;

import koh.game.entities.actors.MonsterGroup;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.SubArea;
import koh.patterns.services.api.Service;

/**
 * Created by Melancholia on 12/28/15.
 */
public abstract class MapMonsterDAO implements Service {

    protected static final int MONSTER_GROUP_PER_MAP = 3;
    protected static final int MONSTER_PER_GROUP = 8;
    protected final int[][] MONSTER_COUNT_BY_DIFFICULTY = new int[][]{ {1, 2, 3}, {4, 5}, {6,7, 8} };

    public abstract MonsterGroup genMonsterGroup(SubArea sub, DofusMap map);

    public abstract void insert(int map, short cell,byte direction, String param1, String param2);

    public abstract void remove(int map, short cell);
}
