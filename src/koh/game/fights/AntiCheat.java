package koh.game.fights;

import java.util.stream.Stream;
import koh.game.entities.actors.character.PlayerInst;
import static koh.game.entities.actors.character.PlayerInst.PProperties;
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
        if (!PlayerInst.PProperties.containsKey(fighter.getID())) {
            PlayerInst.PProperties.put(fighter.getID(), new PlayerInst());
        }
        if (win) {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(fighter.getID()).myVictimIPS.containsKey(((CharacterFighter) Target).character.getAccount().currentIP)) {
                    PProperties.get(fighter.getID()).myVictimIPS.put(((CharacterFighter) Target).character.getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += PProperties.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second;
                        PProperties.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second++;
                    } else {
                        PProperties.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first = System.currentTimeMillis();
                        PProperties.get(fighter.getID()).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second = 1;
                    }
                }
                if (!PProperties.get(fighter.getID()).myVictims.containsKey(Target.getID())) {
                    PProperties.get(fighter.getID()).myVictims.put(Target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.getID()).myVictims.get(Target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(fighter.getID()).myVictims.get(Target.getID()).second) ? bestScore : PProperties.get(fighter.getID()).myVictims.get(Target.getID()).second;
                        PProperties.get(fighter.getID()).myVictims.get(Target.getID()).second++;
                    } else {
                        PProperties.get(fighter.getID()).myVictims.get(Target.getID()).first = System.currentTimeMillis();
                        PProperties.get(fighter.getID()).myVictims.get(Target.getID()).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        } else {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(fighter.getID()).VictimByIPS.containsKey(((CharacterFighter) Target).character.getAccount().currentIP)) {
                    PProperties.get(fighter.getID()).VictimByIPS.put(((CharacterFighter) Target).character.getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.getID()).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += PProperties.get(fighter.getID()).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second;
                        PProperties.get(fighter.getID()).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second++;
                    } else {
                        PProperties.get(fighter.getID()).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first = System.currentTimeMillis();
                        PProperties.get(fighter.getID()).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second = 1;
                    }
                }
                if (!PProperties.get(fighter.getID()).VictimsBy.containsKey(Target.getID())) {
                    PProperties.get(fighter.getID()).VictimsBy.put(Target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).second) ? bestScore : PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).second;
                        PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).second++;
                    } else {
                        PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).first = System.currentTimeMillis();
                        PProperties.get(fighter.getID()).VictimsBy.get(Target.getID()).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        }
        return DeviseBy;
    }

}
