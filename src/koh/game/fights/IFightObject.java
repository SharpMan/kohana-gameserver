package koh.game.fights;

/**
 *
 * @author Neo-Craft
 */
public interface IFightObject {
    
    public Integer Priority();

   

    public enum FightObjectType {

        OBJECT_FIGHTER(1),
        OBJECT_TRAP(2),
        OBJECT_GLYPHE(3),
        OBJECT_CAWOTTE(4);

        public byte value;

        FightObjectType(int t) {
            this.value = (byte) t;
        }
    }

    public FightObjectType ObjectType();

    public short CellId();

    public boolean CanWalk();

    public boolean CanStack();
    
     public boolean CanGoThrough();
    
    public int compareTo(IFightObject obj);

}
