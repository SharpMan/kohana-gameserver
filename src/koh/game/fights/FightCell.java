package koh.game.fights;

import java.util.AbstractQueue;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import koh.game.fights.IFightObject.FightObjectType;
import koh.game.fights.effects.buff.BuffActiveType;
import koh.game.fights.layers.FightActivableObject;
import koh.game.fights.layers.FightBomb;
import koh.game.fights.layers.FightGlyph;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class FightCell {

    protected static final Logger logger = LogManager.getLogger(FightCell.class);

    public class FightCellComparator implements Comparator<IFightObject> {

        @Override
        public int compare(IFightObject o1, IFightObject o2) {
            return o1.compareTo(o2);
        }
    }

    @Getter
    public short Id;
    private boolean myWalkable;
    @Getter
    private boolean lineOfSight;

    private AbstractQueue<IFightObject> myFightObjects = new PriorityBlockingQueue<>(20, new FightCellComparator());
    //Never change this is the heart of trap network

    public FightCell(short Id, boolean walk, boolean los) {
        this.Id = Id;
        this.myWalkable = walk;
        this.lineOfSight = los;
    }

    public int beginTurn(Fighter fighter) {
        for (IFightObject obj : myFightObjects) {
            if (obj instanceof FightActivableObject) {
                final FightActivableObject activableObject = (FightActivableObject) obj;
                if (activableObject.activationType == BuffActiveType.ACTIVE_BEGINTURN || activableObject instanceof FightBomb) {
                    activableObject.loadTargets(fighter);
                    final int result = activableObject.activate(fighter,BuffActiveType.ACTIVE_BEGINTURN);
                    if(result != -1){
                        return result;
                    }
                }
            }
        }

        return -1;
    }
    
    public int endTurn(Fighter fighter){
        for (IFightObject object : myFightObjects) {
            if (object instanceof FightActivableObject) {
                final FightActivableObject activableObject = (FightActivableObject) object;
                if (activableObject.activationType == BuffActiveType.ACTIVE_ENDTURN || activableObject instanceof FightBomb) {
                    activableObject.loadTargets(fighter);
                    final int result = activableObject.activate(fighter,BuffActiveType.ACTIVE_ENDTURN);
                    if(result == -3){
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

    public boolean canGoTrough(){
        return this.myFightObjects.stream().allMatch(obj -> obj.canGoThrough());
    }

    public boolean hasGameObject(FightObjectType objectType) {
        return myFightObjects.stream().anyMatch(x -> x.getObjectType() == objectType);
    }



    public boolean hasGameObject(FightObjectType objectType, FightObjectType objectTyp2) {
        try {
            return myFightObjects.stream().anyMatch(x -> x.getObjectType() == objectType || x.getObjectType() == objectTyp2);
        }
        catch (Exception e){
            logger.error("myfightObject null {} or object {}",myFightObjects == null,myFightObjects.stream().anyMatch(x-> x == null));
            e.printStackTrace();
            return true;
        }
    }

    public <T> T[] getObjectsAs(FightObjectType objecttype) {
        return (T[]) this.myFightObjects.stream().filter(x -> x.getObjectType() == objecttype).map(e -> (T)e).toArray();
    }

    public FightGlyph[] getGlyphes(){
        return myFightObjects.stream()
                .filter(x -> x.getObjectType() == FightObjectType.OBJECT_GLYPHE)
                .map(f -> (FightGlyph) f)
                .toArray(FightGlyph[]::new);
    }

    public Stream<FightGlyph> getGlyphStream(Predicate<? super IFightObject> prdct){
        return myFightObjects.stream()
                .filter(x -> x.getObjectType() == FightObjectType.OBJECT_GLYPHE)
                .filter(prdct)
                .map(f -> (FightGlyph) f);
    }


    public FightGlyph[] getGlyphes(Predicate<? super IFightObject> prdct){
        return myFightObjects.stream()
                .filter(x -> x.getObjectType() == FightObjectType.OBJECT_GLYPHE)
                .filter(prdct)
                .map(f -> (FightGlyph) f)
                .toArray(FightGlyph[]::new);
    }




    public boolean hasGameObject(IFightObject objectType) {
        return myFightObjects.contains(objectType);
    }

    public FightActivableObject[] getObjectsLayer() {
        return this.myFightObjects.stream().filter(x -> x instanceof FightActivableObject)
                .map(obj -> ((FightActivableObject)obj)).toArray(FightActivableObject[]::new);
    }



    public IFightObject[] getObjects(FightObjectType objectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == objectType).toArray(IFightObject[]::new);
    }

    public List<IFightObject> getObjectsAsList(FightObjectType ObjectType) {
        return this.myFightObjects.stream().filter(x -> x.getObjectType() == ObjectType).collect(Collectors.toList());
    }
    
    public boolean hasFighter(){
        return myFightObjects.stream().anyMatch(x -> x instanceof Fighter);
    }


    public Fighter getFighter() {
        return myFightObjects.stream()
                .filter(x -> x instanceof Fighter)
                .map(x -> (Fighter)x)
                .findFirst()
                .orElse(null);
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
        return (this.getObjectsAsFighter()[0].getTeam().id != Team.id && this.getObjectsAsFighter()[0].isAlive()) ? this.getObjectsAsFighter()[0] : null; //Class not id ...
    }

    public Fighter hasFriend(FightTeam Team) {
        if (!this.hasFighter()) {
            return null;
        }
        return (this.getObjectsAsFighter()[0].getTeam().id == Team.id && this.getObjectsAsFighter()[0].isAlive()) ? this.getObjectsAsFighter()[0] : null; //Class not id ...
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
            final Fighter fighter = (Fighter) fightObject;
            for (IFightObject Object : myFightObjects) {
                if (Object instanceof FightActivableObject) {
                    final FightActivableObject activableObject = (FightActivableObject) Object;

                    if (activableObject.activationType == BuffActiveType.ACTIVE_ENDMOVE) {
                        if (!fighter.isDead()) {
                            activableObject.loadTargets(fighter);
                            final int result = activableObject.activate(fighter, BuffActiveType.ACTIVE_ENDMOVE);
                            if(result != -1)
                                return result;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean contains(IFightObject object){
        return this.myFightObjects.contains(object);
    }

    public void removeObject(IFightObject object) {
        this.myFightObjects.remove(object);
    }

    public void clear() {
        try {
            Id = 0;
            myWalkable = false;
            lineOfSight = false;
            myFightObjects.clear();
            myFightObjects = null;
            this.finalize();
        } catch (Throwable tr) {
        }
    }

}
