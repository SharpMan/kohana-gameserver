package koh.game.entities.kolissium.tasks;

import koh.game.actions.GameAction;
import koh.game.actions.GameFight;
import koh.game.entities.actors.Player;
import koh.game.fights.Fight;
import koh.game.fights.FightTeam;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.utils.PeriodicContestExecutor;

import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by Melancholia on 6/10/16.
 */
public class JoinFightTask  implements Runnable {


    private Iterator<Player> toJoin;
    private Fight fight;
    private int guid;
    private ScheduledFuture<?> myFuture;

    public JoinFightTask(PeriodicContestExecutor PeriodicContest, Iterator<Player> toJoin, Fight fight, int guid) {
        this.toJoin = toJoin;
        this.fight = fight;
        this.guid = guid;
        this.myFuture = PeriodicContest.schedulePeriodicTask(this, 750, 500);
    }

    @Override
    public void run() {
        if (toJoin == null || !toJoin.hasNext()) {
            this.cancel();
        } else {
            final Player joiner = toJoin.next();
            if (joiner != null && joiner.isInWorld()) {
                try {
                    synchronized (joiner.getFighterLook()){
                        final FightTeam team = fight.getTeam(guid);
                        final Fighter fighter = new CharacterFighter(fight, joiner.getClient());

                        final GameAction FightAction = new GameFight(fighter, fight);

                        joiner.getClient().addGameAction(FightAction);
                        fight.joinFightTeam(fighter, team, false, (short) -1, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!toJoin.hasNext()) {
                this.cancel();
            }
        }
    }

    public void cancel() {
        try {
            if (myFuture != null && !myFuture.isCancelled()) {
                myFuture.cancel(false);
            }
        } finally {
            if (fight != null) {
                //fight.setTeamJoinedFight(fight.getTeamID(guid) - 1, true);
            }
        }

    }
}
