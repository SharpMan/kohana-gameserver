package koh.game.entities.environments.cells;

/**
 *
 * @author Neo-Craft
 */
public interface IZone {

    public int Surface();

    public byte MinRadius();

    public byte Direction();

    public byte Radius();

    public void SetDirection(byte Direction);

    public void SetRadius(byte Radius);

    public Short[] GetCells(short centerCell);

}
