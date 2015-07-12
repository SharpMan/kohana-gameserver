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

    public Map<Integer, Couple<Long, Integer>> myVictims = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public Map<String, Couple<Long, Integer>> myVictimIPS = Collections.synchronizedMap(new HashMap<String, Couple<Long, Integer>>());
    public Map<Integer, Couple<Long, Integer>> VictimsBy = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public Map<String, Couple<Long, Integer>> VictimByIPS = Collections.synchronizedMap(new HashMap<String, Couple<Long, Integer>>());

    public Map<Integer, Couple<Long, Integer>> VictimeKoli = Collections.synchronizedMap(new HashMap<Integer, Couple<Long, Integer>>());
    public long mutedTime;

    public static Map<Integer, PlayerInst> PProperties = Collections.synchronizedMap(new HashMap<Integer, PlayerInst>());

    public void Clear() {
        try {
            this.myVictims.clear();
            this.myVictimIPS.clear();
            this.VictimeKoli.clear();
            this.VictimsBy = null;
            this.VictimByIPS = null;
            this.VictimeKoli = null;
            this.finalize();
        } catch (Throwable ex) {
        }
    }

}
