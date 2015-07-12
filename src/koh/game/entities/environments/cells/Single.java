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
    public void SetDirection(byte Direction) {
        this.Direction = Direction;
    }

    @Override
    public void SetRadius(byte Radius) {
        this.Radius = Radius;
    }

    @Override
    public int Surface() {
        return 1;
    }

    @Override
    public Short[] GetCells(short centerCell) {
        return new Short[]{
            centerCell
        };
    }

    @Override
    public byte MinRadius() {
        return MinRadius;
    }

    @Override
    public byte Direction() {
        return DirectionsEnum.UP;
    }

    @Override
    public byte Radius() {
        return Radius;
    }

}
