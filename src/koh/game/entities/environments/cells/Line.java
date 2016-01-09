package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class Line implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    public Line(byte radius) {
        this.Radius = radius;
        this.Direction = DirectionsEnum.RIGHT;
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
    public int getSurface() {
        return this.Radius + 1;
    }

    @Override
    public Short[] getCells(short centerCell) {
        MapPoint mapPoint = MapPoint.fromCellId(centerCell);
        ArrayList<Short> list = new ArrayList<>();
        for (int index = (int) this.MinRadius; index <= (int) this.Radius; ++index) {
            switch (this.Direction) {
                case DirectionsEnum.RIGHT:
                    Line.AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() + index, list);
                    break;
                case DirectionsEnum.DOWN_RIGHT:
                    Line.AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y(), list);
                    break;
                case DirectionsEnum.DOWN:
                    Line.AddCellIfValid(mapPoint.get_x() + index, mapPoint.get_y() - index, list);
                    break;
                case DirectionsEnum.DOWN_LEFT:
                    Line.AddCellIfValid(mapPoint.get_x(), mapPoint.get_y() - index, list);
                    break;
                case DirectionsEnum.LEFT:
                    Line.AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() - index, list);
                    break;
                case DirectionsEnum.UP_LEFT:
                    Line.AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y(), list);
                    break;
                case DirectionsEnum.UP:
                    Line.AddCellIfValid(mapPoint.get_x() - index, mapPoint.get_y() + index, list);
                    break;
                case DirectionsEnum.UP_RIGHT:
                    Line.AddCellIfValid(mapPoint.get_x(), mapPoint.get_y() + index, list);
                    break;
            }
        }
        return list.stream().toArray(Short[]::new);
    }

    private static void AddCellIfValid(int x, int y, List<Short> container) {
        if (!MapPoint.IsInMap(x, y)) {
            return;
        }
        container.add(MapPoint.coordToCellId(x, y));
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

}
