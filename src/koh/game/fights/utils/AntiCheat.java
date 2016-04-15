package koh.game.fights.utils;

import java.util.stream.Stream;
import koh.game.entities.actors.character.PlayerInst;
import static koh.game.entities.actors.character.PlayerInst.P_PROPERTIES;

import koh.game.fights.Fighter;
import koh.game.fights.fighters.CharacterFighter;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public class AntiCheat {

    public final static int AGGRO_PER_MIN = 180;

    public static short deviserBy(Stream<Fighter> versus, Fighter fighter, boolean win) {

        short devisedBy = 1;
        if (!PlayerInst.P_PROPERTIES.containsKey(fighter.getID())) {
            PlayerInst.P_PROPERTIES.put(fighter.getID(), new PlayerInst());
        }
        if (win) {
            for (Fighter target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!P_PROPERTIES.get(fighter.getID()).myVictimIPS.containsKey(target.getPlayer().getAccount().currentIP)) {
                    P_PROPERTIES.get(fighter.getID()).myVictimIPS.put(target.getPlayer().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(target.getPlayer().getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(target.getPlayer().getAccount().currentIP).second;
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(target.getPlayer().getAccount().currentIP).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(target.getPlayer().getAccount().currentIP).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(target.getPlayer().getAccount().currentIP).second = 1;
                    }
                }
                if (!P_PROPERTIES.get(fighter.getID()).myVictimsById.containsKey(target.getID())) {
                    P_PROPERTIES.get(fighter.getID()).myVictimsById.put(target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).second;
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(target.getID()).second = 1;
                    }
                }
                devisedBy += bestScore;
            }
        } else {
            for (Fighter target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!P_PROPERTIES.get(fighter.getID()).victimByIPS.containsKey(target.getPlayer().getAccount().currentIP)) {
                    P_PROPERTIES.get(fighter.getID()).victimByIPS.put(target.getPlayer().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).victimByIPS.get(target.getPlayer().getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += P_PROPERTIES.get(fighter.getID()).victimByIPS.get(target.getPlayer().getAccount().currentIP).second;
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get((target.getPlayer()).getAccount().currentIP).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(target.getPlayer().getAccount().currentIP).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(target.getPlayer().getAccount().currentIP).second = 1;
                    }
                }
                if (!P_PROPERTIES.get(fighter.getID()).victimsById.containsKey(target.getID())) {
                    P_PROPERTIES.get(fighter.getID()).victimsById.put(target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    final long time = P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).second;
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(target.getID()).second = 1;
                    }
                }
                devisedBy += bestScore;
            }
        }
        return devisedBy;
    }

}
