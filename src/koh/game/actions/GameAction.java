package koh.game.actions;

import java.lang.reflect.Method;

import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.network.WorldClient;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public abstract class GameAction {

    //private List<Method> myEndCallBacks = new ArrayList<>();

    public abstract boolean canSubAction(GameActionTypeEnum ActionType);

    public GameActionTypeEnum actionType;

    public IGameActor actor;

    public boolean isFinish;

    public GameAction(GameActionTypeEnum actionType, IGameActor actor) {
        this.actor = actor;
        this.actionType = actionType;
    }
    
    public WorldClient getClient(){
        try{
            return ((Player) actor).client;
        }
        catch(Exception e){
            return null;
        }
    }

    public void execute() {
    }

    public void abort(Object[] Args) {
        this.isFinish = true;
    }

    public void endExecute() throws Exception {
        this.isFinish = true;
    }

    public void registerEnd(Method CallBack) {
        //this.myEndCallBacks.add(CallBack);
    }
    
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
