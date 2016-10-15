package koh.game.fights.utils;

import koh.game.entities.actors.character.PlayerInst;
import koh.game.fights.FightTypeEnum;
import koh.game.fights.Fighter;
import koh.utils.Couple;

import java.util.ArrayList;
import java.util.stream.Stream;

import static koh.game.entities.actors.character.PlayerInst.P_PROPERTIES;

/**
 * @author Neo-Craft
 */
public class AntiCheat {

    public final static int AGGRO_PER_MIN = 120;
    private final static int KOLISEO_PER_MIN = 20;


    //TODO LIST IP for losser also
    public static short deviserBy(Stream<Fighter> versus, Fighter fighter, boolean win, FightTypeEnum type) {
        try {
            short devisedBy = 1;
            if (!PlayerInst.P_PROPERTIES.containsKey(fighter.getID())) {
                PlayerInst.P_PROPERTIES.put(fighter.getID(), new PlayerInst());
            }
            if (win) {
                final ArrayList<String> ipsIterated = new ArrayList<>(3);
                for (Fighter target : (Iterable<Fighter>) versus::iterator) {
                    int bestScore = 0;
                    if(!ipsIterated.contains(target.getPlayer().getAccount().currentIP)) {
                        if (!P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).containsKey(target.getPlayer().getAccount().currentIP)) {
                            P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).put(target.getPlayer().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                        } else {
                            long time = P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).get(target.getPlayer().getAccount().currentIP).first;
                            if ((System.currentTimeMillis() - time) < (type == FightTypeEnum.FIGHT_TYPE_AGRESSION ? AGGRO_PER_MIN : KOLISEO_PER_MIN) * 60 * 1000) {
                                bestScore += P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).get(target.getPlayer().getAccount().currentIP).second;
                                P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).get(target.getPlayer().getAccount().currentIP).second++;
                            } else {
                                P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).get(target.getPlayer().getAccount().currentIP).first = System.currentTimeMillis();
                                P_PROPERTIES.get(fighter.getID()).myVictimIPS.get(type).get(target.getPlayer().getAccount().currentIP).second = 1;
                            }
                        }
                        ipsIterated.add(target.getPlayer().getAccount().currentIP);
                    }

                    if (!P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).containsKey(target.getID())) {
                        P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).put(target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                    } else {
                        long time = P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).first;
                        if ((System.currentTimeMillis() - time) < (type == FightTypeEnum.FIGHT_TYPE_AGRESSION ? AGGRO_PER_MIN : KOLISEO_PER_MIN) * 60 * 1000) {
                            bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).second;
                            P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).second++;
                        } else {
                            P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).first = System.currentTimeMillis();
                            P_PROPERTIES.get(fighter.getID()).myVictimsById.get(type).get(target.getID()).second = 1;
                        }
                    }
                    devisedBy += bestScore;
                }
                ipsIterated.clear();
            } else {
                for (Fighter target : (Iterable<Fighter>) versus::iterator) {
                    int bestScore = 0;
                    if (!P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).containsKey(target.getPlayer().getAccount().currentIP)) {
                        P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).put(target.getPlayer().getAccount().currentIP, new Couple<>(System.currentTimeMillis(), 1));
                    } else {
                        long time = P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).get(target.getPlayer().getAccount().currentIP).first;
                        if ((System.currentTimeMillis() - time) < (type == FightTypeEnum.FIGHT_TYPE_AGRESSION ? AGGRO_PER_MIN : KOLISEO_PER_MIN) * 60 * 1000) {
                            bestScore += P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).get(target.getPlayer().getAccount().currentIP).second;
                            P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).get((target.getPlayer()).getAccount().currentIP).second++;
                        } else {
                            P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).get(target.getPlayer().getAccount().currentIP).first = System.currentTimeMillis();
                            P_PROPERTIES.get(fighter.getID()).victimByIPS.get(type).get(target.getPlayer().getAccount().currentIP).second = 1;
                        }
                    }
                    if (!P_PROPERTIES.get(fighter.getID()).victimsById.get(type).containsKey(target.getID())) {
                        P_PROPERTIES.get(fighter.getID()).victimsById.get(type).put(target.getID(), new Couple<>(System.currentTimeMillis(), 1));
                    } else {
                        final long time = P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).first;
                        if ((System.currentTimeMillis() - time) < (type == FightTypeEnum.FIGHT_TYPE_AGRESSION ? AGGRO_PER_MIN : KOLISEO_PER_MIN) * 60 * 1000) {
                            bestScore = (bestScore > P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).second) ? bestScore : P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).second;
                            P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).second++;
                        } else {
                            P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).first = System.currentTimeMillis();
                            P_PROPERTIES.get(fighter.getID()).victimsById.get(type).get(target.getID()).second = 1;
                        }
                    }
                    devisedBy += bestScore;
                }
            }
            return devisedBy;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

}
