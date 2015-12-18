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
        if (!PlayerInst.PProperties.containsKey(fighter.ID)) {
            PlayerInst.PProperties.put(fighter.ID, new PlayerInst());
        }
        if (win) {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(fighter.ID).myVictimIPS.containsKey(((CharacterFighter) Target).character.getAccount().currentIP)) {
                    PProperties.get(fighter.ID).myVictimIPS.put(((CharacterFighter) Target).character.getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.ID).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += PProperties.get(fighter.ID).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second;
                        PProperties.get(fighter.ID).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second++;
                    } else {
                        PProperties.get(fighter.ID).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first = System.currentTimeMillis();
                        PProperties.get(fighter.ID).myVictimIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second = 1;
                    }
                }
                if (!PProperties.get(fighter.ID).myVictims.containsKey(Target.ID)) {
                    PProperties.get(fighter.ID).myVictims.put(Target.ID, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.ID).myVictims.get(Target.ID).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(fighter.ID).myVictims.get(Target.ID).second) ? bestScore : PProperties.get(fighter.ID).myVictims.get(Target.ID).second;
                        PProperties.get(fighter.ID).myVictims.get(Target.ID).second++;
                    } else {
                        PProperties.get(fighter.ID).myVictims.get(Target.ID).first = System.currentTimeMillis();
                        PProperties.get(fighter.ID).myVictims.get(Target.ID).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        } else {
            for (Fighter Target : (Iterable<Fighter>) versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(fighter.ID).VictimByIPS.containsKey(((CharacterFighter) Target).character.getAccount().currentIP)) {
                    PProperties.get(fighter.ID).VictimByIPS.put(((CharacterFighter) Target).character.getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.ID).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore += PProperties.get(fighter.ID).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second;
                        PProperties.get(fighter.ID).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second++;
                    } else {
                        PProperties.get(fighter.ID).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).first = System.currentTimeMillis();
                        PProperties.get(fighter.ID).VictimByIPS.get(((CharacterFighter) Target).character.getAccount().currentIP).second = 1;
                    }
                }
                if (!PProperties.get(fighter.ID).VictimsBy.containsKey(Target.ID)) {
                    PProperties.get(fighter.ID).VictimsBy.put(Target.ID, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(fighter.ID).VictimsBy.get(Target.ID).first;
                    if ((System.currentTimeMillis() - time) < AGGRO_PER_MIN * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(fighter.ID).VictimsBy.get(Target.ID).second) ? bestScore : PProperties.get(fighter.ID).VictimsBy.get(Target.ID).second;
                        PProperties.get(fighter.ID).VictimsBy.get(Target.ID).second++;
                    } else {
                        PProperties.get(fighter.ID).VictimsBy.get(Target.ID).first = System.currentTimeMillis();
                        PProperties.get(fighter.ID).VictimsBy.get(Target.ID).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        }
        return DeviseBy;
    }

}
