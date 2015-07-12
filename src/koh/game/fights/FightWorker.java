package koh.game.fights;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import koh.game.Main;
import koh.protocol.messages.game.context.fight.GameFightNewRoundMessage;

/**
 *
 * @author Neo-Craft
 */
public class FightWorker {

    private Fight Fight;

    private List<Fighter> myFightersTurn = Collections.synchronizedList(new ArrayList<>());
    private Fighter myCurrentFighter;
    public int Round = 0, FightTurn;

    public List<Fighter> Fighters() {
        return this.myFightersTurn;
    }

    public FightWorker(Fight Fight) {
        this.Fight = Fight;
    }

    public void SummonFighter(Fighter fighter) {
        int index = myFightersTurn.indexOf(fighter.Invocator) + 1;
        if (index == 0) {
            index = myFightersTurn.size();
        }
        myFightersTurn.add(index, fighter);
    }

    public void InitTurns() {
        this.myFightersTurn.clear();

        List<Fighter> Team1 = Fight.Fighters().filter(x -> x.Team.Id == 0).sorted((e1, e2) -> Integer.compare(e2.Initiative(false), e1.Initiative(false))).collect(Collectors.toList());
        List<Fighter> Team2 = Fight.Fighters().filter(x -> x.Team.Id == 1).sorted((e1, e2) -> Integer.compare(e2.Initiative(false), e1.Initiative(false))).collect(Collectors.toList());

        for (Fighter Fighter : Team1) {
            int FIndex = Team1.indexOf(Fighter);

            if (Team2.size() - 1 >= FIndex) {
                Fighter OppositeFighter = Team2.get(FIndex);

                if (OppositeFighter.Initiative(false) > Fighter.Initiative(false)) {
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

    public Fighter GetNextFighter() {
        try {
            this.FightTurn++;
            do {
                if (this.myCurrentFighter == null || this.myCurrentFighter == this.myFightersTurn.get(myFightersTurn.size() - 1)) //this.myFightersTurn.LastOrDefault()
                {
                    Round++;
                    this.Fight.sendToField(new GameFightNewRoundMessage(Round));
                    this.myCurrentFighter = this.myFightersTurn.get(0);
                } else {
                    this.myCurrentFighter = this.myFightersTurn.get(this.myFightersTurn.indexOf(this.myCurrentFighter) + 1);
                }
            } while (!this.myCurrentFighter.CanBeginTurn());

            return this.myCurrentFighter;
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (Exception e1) {
            Main.Logs().writeError("FightWorker::GetNextFighter() -> " + e1.getMessage());
            e1.printStackTrace();
            return null;
        }
    }

    public void Dispose() {
        this.myFightersTurn.clear();
        this.Round = 0;
        this.Fight = null;
        this.myCurrentFighter = null;
        try {
            this.myFightersTurn = null;
            this.finalize();
        } catch (Throwable ex) {
        }
    }

}
