package koh.game.actions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;

/**
 *
 * @author Neo-Craft
 */
public abstract class GameAction {

    //private List<Method> myEndCallBacks = new ArrayList<>();

    public abstract boolean CanSubAction(GameActionTypeEnum ActionType);

    public GameActionTypeEnum ActionType;

    public IGameActor Actor;

    public boolean IsFinish;

    public GameAction(GameActionTypeEnum ActionType, IGameActor Actor) {
        this.Actor = Actor;
        this.ActionType = ActionType;
    }
    
    public WorldClient GetClient(){
        try{
            return ((Player) Actor).Client;
        }
        catch(Exception e){
            return null;
        }
    }

    public void Execute() {
    }

    public void Abort(Object[] Args) {
        this.IsFinish = true;
    }

    public void EndExecute() throws Exception {
        this.IsFinish = true;
    }

    public void RegisterEnd(Method CallBack) {
        //this.myEndCallBacks.add(CallBack);
    }

}
