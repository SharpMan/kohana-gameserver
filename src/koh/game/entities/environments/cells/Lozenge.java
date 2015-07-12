package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.maps.pathfinding.MapPoint;

/**
 *
 * @author Neo-Craft
 */
public class Lozenge implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    public Lozenge(byte minRadius, byte radius) {
        this.MinRadius = minRadius;
        this.Radius = radius;
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
        return (((int) this.Radius + 1) * ((int) this.Radius + 1) + (int) this.Radius * (int) this.Radius);
    }

    @Override
    public Short[] GetCells(short centerCell) {
        MapPoint mapPoint = MapPoint.fromCellId(centerCell);
        ArrayList<Short> list = new ArrayList<>();
        if ((int) this.Radius == 0) {
            if ((int) this.MinRadius == 0) {
                list.add(centerCell);
            }
            return list.stream().toArray(Short[]::new);
        } else {
            int x = mapPoint.get_x() - (int) this.Radius;
            int num1 = 0;
            int num2 = 1;
            for (; x <= mapPoint.get_x() + (int) this.Radius; ++x) {
                for (int index = -num1; index <= num1; ++index) {
                    if ((int) this.MinRadius == 0 || Math.abs(mapPoint.get_x() - x) + Math.abs(index) >= (int) this.MinRadius) {
                        Lozenge.AddCellIfValid(x, index + mapPoint.get_y(), list);
                    }
                }
                if (num1 == (int) this.Radius) {
                    num2 = -num2;
                }
                num1 += num2;
            }
            return list.stream().toArray(Short[]::new);
        }
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
