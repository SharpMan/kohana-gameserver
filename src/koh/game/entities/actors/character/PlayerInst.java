package koh.game.entities.actors.character;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import koh.game.entities.actors.Player;
import koh.utils.Couple;
import lombok.Getter;
import lombok.Setter;

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
    @Getter @Setter
    private long mutedTime;
    private Map<Long,Integer> cotes = new TreeMap<>();
    private Map<Long,Boolean> kolizeumWins = new TreeMap<>();

    public static final Map<Integer, PlayerInst> P_PROPERTIES = Collections.synchronizedMap(new HashMap<Integer, PlayerInst>());

    public static PlayerInst getPlayerInst(int id){
        PlayerInst objet = P_PROPERTIES.get(id);
        if(objet == null){
            objet = new PlayerInst();
            P_PROPERTIES.put(id, objet);
        }
        return objet;
    }

    public void updateCote(Player character, boolean win){
        this.cotes.put(Instant.now().toEpochMilli(), character.getKolizeumRate().getRating());
        this.kolizeumWins.put(Instant.now().toEpochMilli(), win);
    }

    public int getDailyWins(){
        return (int) this.kolizeumWins.entrySet()
                .stream()
                .filter(en -> TimeUnit.MILLISECONDS.toHours(Instant.now().minusMillis(en.getKey()).toEpochMilli()) < 24)
                .filter(en -> en.getValue())
                .count();
    }

    public int getDailyFight(){
        return (int) this.kolizeumWins.entrySet()
                .stream()
                .filter(en -> TimeUnit.MILLISECONDS.toHours(Instant.now().minusMillis(en.getKey()).toEpochMilli()) < 24)
                .count();
    }

    public int getDailyCote(){
        return this.cotes.entrySet()
                .stream()
                .filter(en -> TimeUnit.MILLISECONDS.toHours(Instant.now().minusMillis(en.getKey()).toEpochMilli()) < 24)
                .mapToInt(en -> en.getValue())
                .max()
                .orElse(0);
    }

    public static boolean isMuted(int id){
        return P_PROPERTIES.containsKey(id) && P_PROPERTIES.get(id).mutedTime > System.currentTimeMillis();
    }

    public static long muteTime(int id){
        return Instant.now().minusMillis(P_PROPERTIES.get(id).mutedTime).getEpochSecond();
    }

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
