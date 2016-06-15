package koh.game.entities.actors.character;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import koh.game.entities.actors.Player;
import koh.game.fights.FightTypeEnum;
import koh.utils.Couple;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Neo-Craft
 */
public class PlayerInst {

    public Map<FightTypeEnum,Map<Integer, Couple<Long, Integer>>> myVictimsById = Collections.synchronizedMap(new HashMap<FightTypeEnum,Map<Integer, Couple<Long, Integer>>>() {{
        this.put(FightTypeEnum.FIGHT_TYPE_AGRESSION, new HashMap<>());
        this.put(FightTypeEnum.FIGHT_TYPE_PVP_ARENA, new HashMap<>());
    }});
    public Map<FightTypeEnum,Map<String, Couple<Long, Integer>>> myVictimIPS = Collections.synchronizedMap(new HashMap<FightTypeEnum,Map<String, Couple<Long, Integer>>>() {{
        this.put(FightTypeEnum.FIGHT_TYPE_AGRESSION, new HashMap<>());
        this.put(FightTypeEnum.FIGHT_TYPE_PVP_ARENA, new HashMap<>());
    }});
    public Map<FightTypeEnum,Map<Integer, Couple<Long, Integer>>> victimsById = Collections.synchronizedMap(new HashMap<FightTypeEnum,Map<Integer, Couple<Long, Integer>>>() {{
        this.put(FightTypeEnum.FIGHT_TYPE_AGRESSION, new HashMap<>());
        this.put(FightTypeEnum.FIGHT_TYPE_PVP_ARENA, new HashMap<>());
    }});
    public Map<FightTypeEnum,Map<String, Couple<Long, Integer>>> victimByIPS = Collections.synchronizedMap(new HashMap<FightTypeEnum,Map<String, Couple<Long, Integer>>>() {{
        this.put(FightTypeEnum.FIGHT_TYPE_AGRESSION, new HashMap<>());
        this.put(FightTypeEnum.FIGHT_TYPE_PVP_ARENA, new HashMap<>());
    }});

    @Getter @Setter
    private long mutedTime, bannedTime;
    private Map<Long,Integer> cotes = new TreeMap<>();
    @Getter
    private Map<Long,Boolean> kolizeumWins = new TreeMap<>();

    public static final Map<Integer, PlayerInst> P_PROPERTIES = Collections.synchronizedMap(new HashMap<>());

    public static boolean isPresent(int id){
        return P_PROPERTIES.containsKey(id);
    }

    public static PlayerInst getPlayerInst(int id){
        PlayerInst objet = P_PROPERTIES.get(id);
        if(objet == null){
            objet = new PlayerInst();
            P_PROPERTIES.put(id, objet);
        }
        return objet;
    }

    public void updateCote(Player character, boolean win){
        this.cotes.put(Instant.now().toEpochMilli(), character.getKolizeumRate().getScreenRating());
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
            this.myVictimIPS.forEach((k, v) -> v.clear());
            this.myVictimsById.forEach((k, v) -> v.clear());
            this.victimByIPS.forEach((k, v) -> v.clear());
            this.victimsById.forEach((k, v) -> v.clear());
            this.myVictimsById.clear();
            this.myVictimIPS.clear();
            this.victimsById = null;
            this.victimByIPS = null;
            this.finalize();
        } catch (Throwable ex) {
        }
    }

}
