package koh.game.fights;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layer.FightActivableObject;
import koh.game.fights.layer.FightBomb;

/**
 *
 * @author Neo-Craft
 */
public class FightCell {

    public class FightCellComparator implements Comparator<IFightObject> {

        @Override
        public int compare(IFightObject o1, IFightObject o2) {
            return o1.compareTo(o2);
        }
    }

    public short Id;
    private boolean myWalkable;
    private boolean myLineOfSight;

    private AbstractQueue<IFightObject> myFightObjects = new PriorityBlockingQueue<>(20, new FightCellComparator());

    public FightCell(short Id, boolean walk, boolean los) {
        this.Id = Id;
        this.myWalkable = walk;
        this.myLineOfSight = los;
    }

    public int BeginTurn(Fighter fighter) {
        for (IFightObject Object : myFightObjects) {
            if (Object instanceof FightActivableObject) {
                FightActivableObject activableObject = (FightActivableObject) Object;
                if (activableObject.activationType == BuffActiveType.ACTIVE_BEGINTURN || activableObject instanceof FightBomb) {
                    activableObject.loadTargets(fighter);
                    int Result = activableObject.activate(fighter);
                    if(Result == -3){
                        return -3;
                    }
                }
            }
        }

        return -1;
    }
    
    public int EndTurn(Fighter fighter){
        for (IFightObject Object : myFightObjects) {
            if (Object instanceof FightActivableObject) {
                FightActivableObject activableObject = (FightActivableObject) Object;
                if (activableObject.activationType == BuffActiveType.ACTIVE_ENDTURN || activableObject instanceof FightBomb) {
                    activableObject.loadTargets(fighter);
                    int Result = activableObject.activate(fighter);
                    if(Result == -3){
                        return -3;
                    }
                }
            }
        }

        return -1;
    }

    public boolean IsWalkable() {
        return this.myWalkable;
    }

    public boolean HasObject(FightObjectType type) {
        return this.myFightObjects.stream().anyMatch(obj -> obj.getObjectType() == type);
    }

    public boolean CanPutObject() {
        return myWalkable && myFightObjects.stream().filter(obj -> obj.getCellId() == Id).allMatch(obj -> obj.canStack());
    }

    public AbstractQueue<IFightObject> GetObjects() {
        return this.myFightObjects;
    }

    public boolean CanWalk() {
        //return this.myWalkable && !this.HasGameObject(FightObjectType.OBJECT_CAWOTTE) && !this.HasGameObject(FightObjectType.OBJECT_FIGHTER);
        return this.myWalkable && this.myFightObjects.stream().allMatch(obj -> obj.canGoThrough());
    }

    public boolean HasGameObject(FightObjectType ObjectType) {
        return myFightObjects.stream().anyMatch(x -> x.getObjectType() == ObjectType);
    }

    public IFightObject[] GetObjects(FightObjectType ObjectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).toArray(IFightObject[]::new);
    }

    public List<IFightObject> GetObjectsAsList(FightObjectType ObjectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).collect(Collectors.toList());
    }
    
    public boolean hasFighter(){
        return myFightObjects.stream().anyMatch(x -> x instanceof Fighter);
    }

    public Fighter[] GetObjectsAsFighter() {
        return this.myFightObjects.stream().filter(x -> x instanceof Fighter).map(x -> (Fighter) x).toArray(Fighter[]::new);
    }

    public List<Fighter> GetObjectsAsFighterList() {
        return this.myFightObjects.stream().filter(x -> x instanceof Fighter).map(x -> (Fighter) x).collect(Collectors.toList());
    }
    
    public List<Fighter> GetObjectsAsFighterList(Predicate<? super IFightObject> prdct) {
        return this.myFightObjects.stream().filter(prdct).map(x -> (Fighter) x).collect(Collectors.toList());
    }

    public Fighter HasEnnemy(FightTeam Team) {
      if (!this.hasFighter()) {
            return null;
        }
        return (this.GetObjectsAsFighter()[0].team.Id != Team.Id && !this.GetObjectsAsFighter()[0].dead) ? this.GetObjectsAsFighter()[0] : null; //Class not id ...
    }

    public Fighter HasFriend(FightTeam Team) {
        if (!this.hasFighter()) {
            return null;
        }
        return (this.GetObjectsAsFighter()[0].team.Id == Team.Id && !this.GetObjectsAsFighter()[0].dead) ? this.GetObjectsAsFighter()[0] : null; //Class not id ...
    }

    public synchronized int AddObject(IFightObject fightObject) {
        return AddObject(fightObject, true);
    }

    public synchronized int AddObject(IFightObject fightObject, boolean runEvent) {
        if (!this.myFightObjects.contains(fightObject)) {
            this.myFightObjects.add(fightObject);
        }
        if (runEvent) {
            return onObjectAdded(fightObject);
        }

        return -1;
    }

    public int onObjectAdded(IFightObject fightObject) {
        if (fightObject instanceof Fighter) {
            Fighter fighter = (Fighter) fightObject;
            for (IFightObject Object : myFightObjects) {
                if (Object instanceof FightActivableObject) {
                    FightActivableObject activableObject = (FightActivableObject) Object;

                    if (activableObject.activationType == BuffActiveType.ACTIVE_ENDMOVE) {
                        if (!fighter.isDead()) {
                            activableObject.loadTargets(fighter);
                            int Result = activableObject.activate(fighter);
                            if(Result == -3)
                                return Result;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public void RemoveObject(IFightObject Object) {
        this.myFightObjects.remove(Object);
    }

    public void Clear() {
        try {
            Id = 0;
            myWalkable = false;
            myLineOfSight = false;
            myFightObjects.clear();
            myFightObjects = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
