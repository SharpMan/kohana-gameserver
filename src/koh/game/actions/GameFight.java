package koh.game.actions;

import koh.game.fights.Fight;
import koh.game.fights.Fighter;
import koh.game.fights.fighters.CharacterFighter;
import koh.game.fights.types.ChallengeFight;

/**
 *
 * @author Neo-Craft
 */
public class GameFight extends GameAction {

    public Fight fight;

    public GameFight(Fighter fighter, Fight fight) {
        super(GameActionTypeEnum.FIGHT, fighter);
        this.fight = fight;
    }

    @Override
    public void abort(Object[] args) {
        if (fight instanceof ChallengeFight) {
            this.fight.leaveFight((Fighter) actor);
        }
        else{
            if(args != null && (boolean) args[0] == false){
                fight.leaveFight((Fighter) actor);
            }
            else
                fight.disconnect((CharacterFighter) actor);
        }
        
        super.abort(args);
    }

    @Override
    public boolean canSubAction(GameActionTypeEnum ActionType) {
        switch (ActionType) {
            case MAP_MOVEMENT:
                return true;
        }
        return false;
    }

}
