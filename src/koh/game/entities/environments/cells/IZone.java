package koh.game.entities.environments.cells;

/**
 *
 * @author Neo-Craft
 */
public interface IZone {

    public int getSurface();

    public byte getMinRadius();

    public byte getDirection();

    public byte getRadius();

    public void setDirection(byte Direction);

    public void setRadius(byte Radius);

    public Short[] getCells(short centerCell);

}
