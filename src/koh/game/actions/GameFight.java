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

    public Fight Fight;

    public GameFight(Fighter Fighter, Fight Fight) {
        super(GameActionTypeEnum.FIGHT, Fighter);
        this.Fight = Fight;
    }

    @Override
    public void Abort(Object[] Args) {
        if (Fight instanceof ChallengeFight) {
            this.Fight.LeaveFight((Fighter) Actor);
        }
        else{
            Fight.Disconnect((CharacterFighter) Actor);
        }
        
        super.Abort(Args);
    }

    @Override
    public boolean CanSubAction(GameActionTypeEnum ActionType) {
        switch (ActionType) {
            case MAP_MOVEMENT:
                return true;
        }
        return false;
    }

}
