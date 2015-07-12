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

    public final static int AggroPerMin = 180;

    public static short DeviserBy(Stream<Fighter> Versus, Fighter Fighter, boolean Win) {
        short DeviseBy = 1;
        if (!PlayerInst.PProperties.containsKey(Fighter.ID)) {
            PlayerInst.PProperties.put(Fighter.ID, new PlayerInst());
        }
        if (Win) {
            for (Fighter Target : (Iterable<Fighter>) Versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(Fighter.ID).myVictimIPS.containsKey(((CharacterFighter) Target).Character.Account.CurrentIP)) {
                    PProperties.get(Fighter.ID).myVictimIPS.put(((CharacterFighter) Target).Character.Account.CurrentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(Fighter.ID).myVictimIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).first;
                    if ((System.currentTimeMillis() - time) < AggroPerMin * 60 * 1000) {
                        bestScore += PProperties.get(Fighter.ID).myVictimIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second;
                        PProperties.get(Fighter.ID).myVictimIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second++;
                    } else {
                        PProperties.get(Fighter.ID).myVictimIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).first = System.currentTimeMillis();
                        PProperties.get(Fighter.ID).myVictimIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second = 1;
                    }
                }
                if (!PProperties.get(Fighter.ID).myVictims.containsKey(Target.ID)) {
                    PProperties.get(Fighter.ID).myVictims.put(Target.ID, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(Fighter.ID).myVictims.get(Target.ID).first;
                    if ((System.currentTimeMillis() - time) < AggroPerMin * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(Fighter.ID).myVictims.get(Target.ID).second) ? bestScore : PProperties.get(Fighter.ID).myVictims.get(Target.ID).second;
                        PProperties.get(Fighter.ID).myVictims.get(Target.ID).second++;
                    } else {
                        PProperties.get(Fighter.ID).myVictims.get(Target.ID).first = System.currentTimeMillis();
                        PProperties.get(Fighter.ID).myVictims.get(Target.ID).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        } else {
            for (Fighter Target : (Iterable<Fighter>) Versus::iterator) {
                int bestScore = 0;
                if (!PProperties.get(Fighter.ID).VictimByIPS.containsKey(((CharacterFighter) Target).Character.Account.CurrentIP)) {
                    PProperties.get(Fighter.ID).VictimByIPS.put(((CharacterFighter) Target).Character.Account.CurrentIP, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(Fighter.ID).VictimByIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).first;
                    if ((System.currentTimeMillis() - time) < AggroPerMin * 60 * 1000) {
                        bestScore += PProperties.get(Fighter.ID).VictimByIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second;
                        PProperties.get(Fighter.ID).VictimByIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second++;
                    } else {
                        PProperties.get(Fighter.ID).VictimByIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).first = System.currentTimeMillis();
                        PProperties.get(Fighter.ID).VictimByIPS.get(((CharacterFighter) Target).Character.Account.CurrentIP).second = 1;
                    }
                }
                if (!PProperties.get(Fighter.ID).VictimsBy.containsKey(Target.ID)) {
                    PProperties.get(Fighter.ID).VictimsBy.put(Target.ID, new Couple<>(System.currentTimeMillis(), 1));
                } else {
                    long time = PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).first;
                    if ((System.currentTimeMillis() - time) < AggroPerMin * 60 * 1000) {
                        bestScore = (bestScore > PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).second) ? bestScore : PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).second;
                        PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).second++;
                    } else {
                        PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).first = System.currentTimeMillis();
                        PProperties.get(Fighter.ID).VictimsBy.get(Target.ID).second = 1;
                    }
                }
                DeviseBy += bestScore;
            }
        }
        return DeviseBy;
    }

}
