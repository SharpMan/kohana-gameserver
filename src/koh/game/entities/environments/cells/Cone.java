package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class Cone implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    public Cone(byte minRadius, byte radius) {
        this.MinRadius = minRadius;
        this.Radius = radius;
    }

    @Override
    public int getSurface() {
        return (int) (Math.pow((this.getRadius() + 1), 2));
    }

    @Override
    public Short[] getCells(short centerCell) {
        MapPoint mapPoint = MapPoint.fromCellId(centerCell);
        ArrayList<Short> list = new ArrayList<>();
        if ((int) this.getRadius() == 0) {
            if ((int) this.MinRadius == 0) {
                list.add(centerCell);
            }
            return list.toArray(new Short[list.size()]);
        } else {
            int num1 = 0;
            int num2 = 1;
            switch (this.Direction) {
                case DirectionsEnum.DOWN_RIGHT:
                    for (int x = mapPoint.get_x(); x <= mapPoint.get_x() + (int) this.getRadius(); ++x) {
                        for (int index = -num1; index <= num1; ++index) {
                            if ((int) this.MinRadius == 0 || Math.abs(mapPoint.get_x() - x) + Math.abs(index) >= (int) this.MinRadius) {
                                Cone.AddCellIfValid(x, index + mapPoint.get_y(), list);
                            }
                        }
                        num1 += num2;
                    }
                    break;
                case DirectionsEnum.DOWN_LEFT:
                    for (int y = mapPoint.get_y(); y >= mapPoint.get_y() - (int) this.getRadius(); --y) {
                        for (int index = -num1; index <= num1; ++index) {
                            if ((int) this.MinRadius == 0 || Math.abs(index) + Math.abs(mapPoint.get_y() - y) >= (int) this.MinRadius) {
                                Cone.AddCellIfValid(index + mapPoint.get_x(), y, list);
                            }
                        }
                        num1 += num2;
                    }
                    break;
                case DirectionsEnum.UP_LEFT:
                    for (int x = mapPoint.get_x(); x >= mapPoint.get_x() - (int) this.getRadius(); --x) {
                        for (int index = -num1; index <= num1; ++index) {
                            if ((int) this.MinRadius == 0 || Math.abs(mapPoint.get_x() - x) + Math.abs(index) >= (int) this.MinRadius) {
                                Cone.AddCellIfValid(x, index + mapPoint.get_y(), list);
                            }
                        }
                        num1 += num2;
                    }
                    break;
                case DirectionsEnum.UP_RIGHT:
                    /*for (int y = mapPoint.get_y(); y <= mapPoint.get_y() - (int) this.getRadius(); ++y) {
                     for (int index = -num1; index <= num1; ++index) {
                     if ((int) this.getMinRadius == 0 || Math.abs(index) + Math.abs(mapPoint.get_y() - y) >= (int) this.getMinRadius) {
                     Cone.AddCellIfValid(index + mapPoint.get_x(), y, list);
                     }
                     }
                     num1 += num2;
                     }
                     break;*/
                    int i = 0;
                    int j = mapPoint.get_y();
                    while (j <= (mapPoint.get_y() + this.getRadius())) {
                        i = -(num1);
                        while (i <= num1) {
                            if ((((int) this.MinRadius == 0) || (((Math.abs(i) + Math.abs((mapPoint.get_y() - j))) >= this.getMinRadius())))) {
                                if (MapPoint.isInMap((i + mapPoint.get_x()), j)) {
                                    Cone.AddCellIfValid(i + mapPoint.get_x(), j, list);
                                };
                            };
                            i++;
                        };
                        num1 += num2;
                        j++;
                    }
                    break;
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

}
