package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class CrossZone implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    public boolean OnlyPerpendicular;
    public boolean AllDirections, Diagonal;
    public List<Byte> disabledDirection;

    public CrossZone(byte minRadius, byte radius) {
        this.MinRadius = minRadius;
        this.Radius = radius;
        this.disabledDirection = new ArrayList<>();
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
    public int Surface() {
        return ((int) this.Radius * 4 + 1);
    }

    @Override
    public Short[] GetCells(short centerCell) {
        ArrayList<Short> list1 = new ArrayList<>();
        if ((int) this.MinRadius == 0) {
            list1.add(centerCell);
        }
        if (this.OnlyPerpendicular) {
            switch (this.Direction) {
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
        for (int index = (int) this.Radius; index > 0; --index) {
            if (index >= (int) this.MinRadius) {
                if (!this.Diagonal) {
                    if (!disabledDirection.contains(DirectionsEnum.DOWN_RIGHT)) {
                        AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y(), list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP_LEFT)) {
                        AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y(), list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP_RIGHT)) {
                        AddCellIfValid(mapPoint.get_x(), mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.DOWN_LEFT)) {
                        AddCellIfValid(mapPoint.get_x(), mapPoint.get_y() - index, list1);
                    }
                }
                if (this.Diagonal || this.AllDirections) {
                    if (!disabledDirection.contains(DirectionsEnum.DOWN)) {
                        AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() - index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.UP)) {
                        AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.RIGHT)) {
                        AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() + index, list1);
                    }
                    if (!disabledDirection.contains(DirectionsEnum.LEFT)) {
                        AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() - index, list1);
                    }
                }
            }
        }
        return list1.stream().toArray(Short[]::new);
    }

    private static void AddCellIfValid(int x, int y, List<Short> container) {
        if (!MapPoint.IsInMap(x, y)) {
            return;
        }
        container.add(MapPoint.CoordToCellId(x, y));
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

}
