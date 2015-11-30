package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class Single implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    @Override
    public void setDirection(byte Direction) {
        this.Direction = Direction;
    }

    @Override
    public void setRadius(byte Radius) {
        this.Radius = Radius;
    }

    @Override
    public int getSurface() {
        return 1;
    }

    @Override
    public Short[] getCells(short centerCell) {
        return new Short[]{
            centerCell
        };
    }

    @Override
    public byte getMinRadius() {
        return MinRadius;
    }

    @Override
    public byte getDirection() {
        return DirectionsEnum.UP;
    }

    @Override
    public byte getRadius() {
        return Radius;
    }

}
