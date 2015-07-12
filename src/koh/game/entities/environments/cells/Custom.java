package koh.game.entities.environments.cells;

/**
 *
 * @author Neo-Craft
 */
public class Custom implements IZone {
    
    
    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    @Override
    public int Surface() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
    public byte MinRadius() {
        return MinRadius;
    }

    @Override
    public byte Direction() {
        return Direction;
    }

    @Override
    public byte Radius() {
        return Radius;
    }

    @Override
    public void SetDirection(byte Direction) {
        this.Direction = Direction;
    }

    @Override
    public void SetRadius(byte Radius) {
       this.Radius = Radius;
    }

    @Override
    public Short[] GetCells(short centerCell) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
