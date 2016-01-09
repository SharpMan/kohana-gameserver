package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public interface IFightObject {
    
    public Integer getPriority();

   

    public enum FightObjectType {

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

    public FightObjectType getObjectType();

    public short getCellId();

    public boolean canWalk();

    public boolean canStack();
    
     public boolean canGoThrough();
    
    public int compareTo(IFightObject obj);

}
