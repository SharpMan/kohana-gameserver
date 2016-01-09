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
    public int getSurface() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   @Override
    public byte getMinRadius() {
        return MinRadius;
    }

    @Override
    public byte getDirection() {
        return Direction;
    }

    @Override
    public byte getRadius() {
        return Radius;
    }

    @Override
    public void setDirection(byte Direction) {
        this.Direction = Direction;
    }

    @Override
    public void setRadius(byte Radius) {
       this.Radius = Radius;
    }

    @Override
    public Short[] getCells(short centerCell) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
