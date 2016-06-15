package koh.game.fights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import koh.protocol.messages.game.context.fight.GameFightNewRoundMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class FightWorker {

    private static final Logger logger = LogManager.getLogger(FightWorker.class);

    private Fight fight;

    private List<Fighter> myFightersTurn = Collections.synchronizedList(new ArrayList<>());
    private Fighter myCurrentFighter;
    public int round = 0, fightTurn;

    public List<Fighter> fighters() {
        return this.myFightersTurn;
    }

    public FightWorker(Fight Fight) {
        this.fight = Fight;
    }

    public synchronized void summonFighter(Fighter fighter) {
        int index = myFightersTurn.indexOf(fighter.getSummoner()) + 1;
        if (index == 0) {
            index = myFightersTurn.size();
        }
        myFightersTurn.add(index, fighter);
    }

    public void initTurns() {
        this.myFightersTurn.clear();
        final List<Fighter> team1 = fight.fighters().filter(x -> x.getTeam().id == 0).sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false))).collect(Collectors.toList());
        final List<Fighter> team2 = fight.fighters().filter(x -> x.getTeam().id == 1).sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false))).collect(Collectors.toList());

        try {
            final List<Fighter> strongTeam = team2.isEmpty() || team1.get(0).getInitiative(false) > team2.get(0).getInitiative(false) ? team1 : team2;
            final Iterator<Fighter> noobTeam = (team2.isEmpty() || team1.get(0).getInitiative(false) > team2.get(0).getInitiative(false) ? team2 : team1).iterator();

            for (final Fighter fighter : strongTeam) {
                this.myFightersTurn.add(fighter);
                if (noobTeam.hasNext()) {
                    this.myFightersTurn.add(noobTeam.next());
                }
            }

            while (noobTeam.hasNext()) {
                this.myFightersTurn.add(noobTeam.next());
            }

        }
        catch (Exception e){
            this.myFightersTurn.clear();
            for (final Fighter fighter : team1) {
                final int FIndex = team1.indexOf(fighter);

                if (team2.size() - 1 >= FIndex) {
                    final Fighter oppositeFighter = team2.get(FIndex);

                    if (oppositeFighter.getInitiative(false) > fighter.getInitiative(false)) {
                        myFightersTurn.add(oppositeFighter);
                        myFightersTurn.add(fighter);
                    } else {
                        myFightersTurn.add(fighter);
                        myFightersTurn.add(oppositeFighter);
                    }
                } else {
                    myFightersTurn.add(fighter);
                }
            }

            for (Fighter Fighter : team2) {
                if (!this.myFightersTurn.contains(Fighter)) {
                    myFightersTurn.add(Fighter);
                }
            }
        }

    }

    public Fighter getNextFighter() {
        try {
            this.fightTurn++;
            do {
                if (this.myCurrentFighter == null || this.myCurrentFighter == this.myFightersTurn.get(myFightersTurn.size() - 1)) //this.myFightersTurn.LastOrDefault()
                {
                    round++;
                    this.fight.sendToField(new GameFightNewRoundMessage(round));
                    this.myCurrentFighter = this.myFightersTurn.get(0);
                } else {
                    this.myCurrentFighter = this.myFightersTurn.get(this.myFightersTurn.indexOf(this.myCurrentFighter) + 1);
                }
            } while (!this.myCurrentFighter.canBeginTurn());

            return this.myCurrentFighter;
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (Exception e1) {
            logger.error("FightWorker::getNextFighter() ->  {}" , e1.getMessage());
            e1.printStackTrace();
            return null;
        }
    }

    public void dispose() {
        this.myFightersTurn.clear();
        this.round = 0;
        this.fight = null;
        this.myCurrentFighter = null;
        try {
            this.myFightersTurn = null;
            this.finalize();
        } catch (Throwable ex) {
        }
    }

}
