package koh.game.fights;

import java.util.ArrayList;
import java.util.Collections;
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

    public void summonFighter(Fighter fighter) {
        int index = myFightersTurn.indexOf(fighter.getSummoner()) + 1;
        if (index == 0) {
            index = myFightersTurn.size();
        }
        myFightersTurn.add(index, fighter);
    }

    public void initTurns() {
        this.myFightersTurn.clear();

        List<Fighter> Team1 = fight.fighters().filter(x -> x.getTeam().id == 0).sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false))).collect(Collectors.toList());
        List<Fighter> Team2 = fight.fighters().filter(x -> x.getTeam().id == 1).sorted((e1, e2) -> Integer.compare(e2.getInitiative(false), e1.getInitiative(false))).collect(Collectors.toList());

        for (Fighter Fighter : Team1) {
            int FIndex = Team1.indexOf(Fighter);

            if (Team2.size() - 1 >= FIndex) {
                Fighter OppositeFighter = Team2.get(FIndex);

                if (OppositeFighter.getInitiative(false) > Fighter.getInitiative(false)) {
                    myFightersTurn.add(OppositeFighter);
                    myFightersTurn.add(Fighter);
                } else {
                    myFightersTurn.add(Fighter);
                    myFightersTurn.add(OppositeFighter);
                }
            } else {
                myFightersTurn.add(Fighter);
            }
        }

        for (Fighter Fighter : Team2) {
            if (!this.myFightersTurn.contains(Fighter)) {
                myFightersTurn.add(Fighter);
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
