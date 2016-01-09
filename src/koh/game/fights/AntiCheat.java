package koh.game.fights;

import java.util.stream.Stream;
import koh.game.entities.actors.character.PlayerInst;
import static koh.game.entities.actors.character.PlayerInst.P_PROPERTIES;
import koh.game.fights.fighters.CharacterFighter;
import koh.utils.Couple;

/**
 *
 * @author Neo-Craft
 */
public class AntiCheat {

    public final static int AGGRO_PER_MIN = 180;

    public static short deviserBy(Stream<Fighter> versus, Fighter fighter, boolean win) {
        short DeviseBy = 1;
        if (!PlayerInst.P_PROPERTIES.containsKey(fighter.getID())) {
            PlayerInst.P_PROPERTIES.put(fighter.getID(), new PlayerInst());
        }
        if (win) {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!P_PROPERTIES.get(fighter.getID()).myVictimIPS.containsKey(((CharacterFighter) Target).getCharacter().getAccount().currentIP)) {
                    P_PROPERTIES.get(fighter.getID()).myVictimIPS.put(((CharacterFighter) Target).getCharacter().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second;
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second = 1;
                    }
                }
                if (!P_PROPERTIES.get(fighter.getID()).myVictimsById.containsKey(Target.getID())) {
                    P_PROPERTIES.get(fighter.getID()).myVictimsById.put(Target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).second;
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(Target.getID()).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        } else {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!P_PROPERTIES.get(fighter.getID()).victimByIPS.containsKey(((CharacterFighter) Target).getCharacter().getAccount().currentIP)) {
                    P_PROPERTIES.get(fighter.getID()).victimByIPS.put(((CharacterFighter) Target).getCharacter().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).victimByIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += P_PROPERTIES.get(fighter.getID()).victimByIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second;
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(((CharacterFighter) Target).getCharacter().getAccount().currentIP).second = 1;
                    }
                }
                if (!P_PROPERTIES.get(fighter.getID()).victimsById.containsKey(Target.getID())) {
                    P_PROPERTIES.get(fighter.getID()).victimsById.put(Target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).second;
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).second++;
                    } else {
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).first = System.currentTimeMillis();
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(Target.getID()).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        }
        return DeviseBy;
    }

}
