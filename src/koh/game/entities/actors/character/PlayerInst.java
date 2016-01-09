package koh.game.entities.actors.character;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public class PlayerInst {

    public Map<Integer, Couple<Long, Integer>> myVictimsById = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public Map<String, Couple<Long, Integer>> myVictimIPS = Collections.synchronizedMap(new HashMap<String, Couple<Long, Integer>>());
    public Map<Integer, Couple<Long, Integer>> victimsById = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public Map<String, Couple<Long, Integer>> victimByIPS = Collections.synchronizedMap(new HashMap<String, Couple<Long, Integer>>());

    public Map<Integer, Couple<Long, Integer>> kolizeumVictims = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public long mutedTime;

    public static final Map<Integer, PlayerInst> P_PROPERTIES = Collections.synchronizedMap(new HashMap<Integer, PlayerInst>());

    public void clear() {
        try {
            this.myVictimsById.clear();
            this.myVictimIPS.clear();
            this.kolizeumVictims.clear();
            this.victimsById = null;
            this.victimByIPS = null;
            this.kolizeumVictims = null;
            this.finalize();
        } catch (Throwable ex) {
        }
    }

}
