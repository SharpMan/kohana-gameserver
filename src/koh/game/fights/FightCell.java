package koh.game.fights;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightBomb;

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
    //Never change this is the heart of trap network

    public FightCell(short Id, boolean walk, boolean los) {
        this.Id = Id;
        this.myWalkable = walk;
        this.myLineOfSight = los;
    }

    public int beginTurn(Fighter fighter) {
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
    
    public int endTurn(Fighter fighter){
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

    public boolean isWalkable() {
        return this.myWalkable;
    }

    public boolean hasObject(FightObjectType type) {
        return this.myFightObjects.stream().anyMatch(obj -> obj.getObjectType() == type);
    }

    public boolean canPutObject() {
        return myWalkable && myFightObjects.stream().filter(obj -> obj.getCellId() == Id).allMatch(obj -> obj.canStack());
    }

    public AbstractQueue<IFightObject> getObjects() {
        return this.myFightObjects;
    }

    public boolean canWalk() {
        //return this.myWalkable && !this.hasGameObject(FightObjectType.OBJECT_CAWOTTE) && !this.hasGameObject(FightObjectType.OBJECT_FIGHTER);
        return this.myWalkable && this.myFightObjects.stream().allMatch(obj -> obj.canGoThrough());
    }

    public boolean hasGameObject(FightObjectType objectType) {
        return myFightObjects.stream().anyMatch(x -> x.getObjectType() == objectType);
    }



    public boolean hasGameObject(FightObjectType objectType, FightObjectType objectTyp2) {
        return myFightObjects.stream().anyMatch(x -> x.getObjectType() == objectType || x.getObjectType() == objectTyp2);
    }

    public <T extends IFightObject> T[] getObjecs(FightObjectType ObjectType) { //TODO use this shit
        return (T[]) this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).map(e -> (T)e).toArray();
    }

    public boolean hasGameObject(IFightObject objectType) {
        return myFightObjects.contains(objectType);
    }

    public IFightObject[] getObjects(FightObjectType ObjectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).toArray(IFightObject[]::new);
    }

    public List<IFightObject> getObjectsAsList(FightObjectType ObjectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).collect(Collectors.toList());
    }
    
    public boolean hasFighter(){
        return myFightObjects.stream().anyMatch(x -> x instanceof Fighter);
    }

    public Fighter[] getObjectsAsFighter() {
        return this.myFightObjects.stream().filter(x -> x instanceof Fighter).map(x -> (Fighter) x).toArray(Fighter[]::new);
    }

    public List<Fighter> getObjectsAsFighterList() {
        return this.myFightObjects.stream().filter(x -> x instanceof Fighter).map(x -> (Fighter) x).collect(Collectors.toList());
    }
    
    public List<Fighter> getObjectsAsFighterList(Predicate<? super IFightObject> prdct) {
        return this.myFightObjects.stream().filter(prdct).map(x -> (Fighter) x).collect(Collectors.toList());
    }

    public Fighter hasEnnemy(FightTeam Team) {
      if (!this.hasFighter()) {
            return null;
        }
        return (this.getObjectsAsFighter()[0].getTeam().id != Team.id && !this.getObjectsAsFighter()[0].isMarkedDead()) ? this.getObjectsAsFighter()[0] : null; //Class not id ...
    }

    public Fighter hasFriend(FightTeam Team) {
        if (!this.hasFighter()) {
            return null;
        }
        return (this.getObjectsAsFighter()[0].getTeam().id == Team.id && !this.getObjectsAsFighter()[0].isMarkedDead()) ? this.getObjectsAsFighter()[0] : null; //Class not id ...
    }

    public synchronized int addObject(IFightObject fightObject) {
        return addObject(fightObject, true);
    }

    public synchronized int addObject(IFightObject fightObject, boolean runEvent) {
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

    public void removeObject(IFightObject Object) {
        this.myFightObjects.remove(Object);
    }

    public void clear() {
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
