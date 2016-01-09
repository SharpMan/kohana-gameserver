package koh.game.entities.environments;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.environments.cells.IZone;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class CrossZone implements IZone {

    public byte minRadius;

    public byte direction;

    public byte radius;

    public boolean onlyPerpendicular;
    public boolean allDirections, diagonal;
    public final List<Byte> disabledDirection;

    public CrossZone(byte minRadius, byte radius) {
        this.minRadius = minRadius;
        this.radius = radius;
        this.disabledDirection = new ArrayList<>();
    }

    @Override
    public void setDirection(byte Direction) {
        this.direction = Direction;
    }

    @Override
    public void setRadius(byte Radius) {
        this.radius = Radius;
    }

    @Override
    public int getSurface() {
        return ((int) this.radius * 4 + 1);
    }

    @Override
    public Short[] getCells(short centerCell) {
        ArrayList<Short> list1 = new ArrayList<>();
        if ((int) this.minRadius == 0) {
            list1.add(centerCell);
        }
        if (this.onlyPerpendicular) {
            switch (this.direction) {
                case DirectionsEnum.DOWN_RIGHT:
                case DirectionsEnum.UP_LEFT:
                    disabledDirection.add(DirectionsEnum.DOWN_RIGHT);
                    disabledDirection.add(DirectionsEnum.UP_LEFT);
                    break;
                case DirectionsEnum.UP_RIGHT:
                case DirectionsEnum.DOWN_LEFT:
                    disabledDirection.add(DirectionsEnum.UP_RIGHT);
                    disabledDirection.add(DirectionsEnum.DOWN_LEFT);
                    break;
                case DirectionsEnum.DOWN:
                case DirectionsEnum.UP:
                    disabledDirection.add(DirectionsEnum.DOWN);
                    disabledDirection.add(DirectionsEnum.UP);
                    break;
                case DirectionsEnum.RIGHT:
                case DirectionsEnum.LEFT:
                    disabledDirection.add(DirectionsEnum.RIGHT);
                    disabledDirection.add(DirectionsEnum.LEFT);
                    break;
            }
        }
        MapPoint mapPoint = MapPoint.fromCellId(centerCell);
        for (int index = (int) this.radius; index > 0; --index) {
            if (index >= (int) this.minRadius) {
                if (!this.diagonal) {
                    if (!disabledDirection.contains(DirectionsEnum.DOWN_RIGHT)) {
                        addCellIfValid(mapPoint.get_x() + index, mapPoint.get_y(), list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP_LEFT)) {
                        addCellIfValid(mapPoint.get_x() - index, mapPoint.get_y(), list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP_RIGHT)) {
                        addCellIfValid(mapPoint.get_x(), mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.DOWN_LEFT)) {
                        addCellIfValid(mapPoint.get_x(), mapPoint.get_y() - index, list1);
                    }
                }
                if (this.diagonal || this.allDirections) {
                    if (!disabledDirection.contains(DirectionsEnum.DOWN)) {
                        addCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() - index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP)) {
                        addCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.RIGHT)) {
                        addCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.LEFT)) {
                        addCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() - index, list1);
                    }
                }
            }
        }
        return list1.stream().toArray(Short[]::new);
    }

    private static void addCellIfValid(int x, int y, List<Short> container) {
        if (!MapPoint.IsInMap(x, y)) {
            return;
        }
        container.add(MapPoint.coordToCellId(x, y));
    }

    @Override
    public byte getMinRadius() {
        return minRadius;
    }

    @Override
    public byte getDirection() {
        return direction;
    }

    @Override
    public byte getRadius() {
        return radius;
    }

}
