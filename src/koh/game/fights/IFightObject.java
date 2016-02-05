package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public interface IFightObject {
    
    Integer getPriority();

   

    enum FightObjectType {

        OBJECT_FIGHTER(1),
        OBJECT_TRAP(2),
        OBJECT_GLYPHE(3),
        OBJECT_STATIC(4),
        OBJECT_BOMB(5),
        OBJECT_PORTAL(6);

        public byte value;

        FightObjectType(int t) {
            this.value = (byte) t;
        }
    }

    FightObjectType getObjectType();

    short getCellId();

    boolean canWalk();

    boolean canStack();
    
    boolean canGoThrough();
    
    int compareTo(IFightObject obj);

}
