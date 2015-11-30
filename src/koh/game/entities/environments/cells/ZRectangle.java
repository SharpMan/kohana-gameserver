package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.maps.pathfinding.MapPoint;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 * @author Neo-Craft
 */
public class ZRectangle implements IZone {

    public byte Direction;

    public byte Radius = 0;

    public byte _radius2;
    public byte _minRadius = 2;

    public boolean diagonalFree;
    public DofusMap Map;

    public ZRectangle(byte minRadius, byte nWidth, byte nHeight,DofusMap Map) {
        this._minRadius = minRadius;
        this.Radius = nWidth;
        this._radius2 = ((nHeight != 0) ? nHeight : nWidth);
        this.Map = Map;
    }

    @Override
    public int getSurface() {
        return (int) (Math.pow(((this.Radius + this._radius2) + 1), 2));
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
        MapPoint origin = MapPoint.fromCellId(centerCell);
        ArrayList<Short> list = new ArrayList<>();

        int i;
        int j;
        int x = origin.get_x();
        int y = origin.get_y();
        if ((((this.getRadius() == 0)) || ((this._radius2 == 0)))) {
            if ((((this.getMinRadius() == 0)) && (!(this.diagonalFree)))) {
                list.add(centerCell);
            };
            return list.stream().toArray(Short[]::new);
        };
        i = (x - this.getRadius());
        while (i <= (x + this.getRadius())) {
            j = (y - this._radius2);
            while (j <= (y + this._radius2)) {
               // System.out.println("sss");
                if ((/*(!(this._minRadius != -1)) ||*/(((Math.abs((x - i)) + Math.abs((y - j))) >= this._minRadius)))) {
                    if (((!(this.diagonalFree)) || (!((Math.abs((x - i)) == Math.abs((y - j))))))) {
                        if (MapPoint.isInMap(i, j)) {
                            this.AddCellIfValid(i, j, list);
                        };
                    };
                };
                j++;
            };
            i++;
        };

        return list.stream().toArray(Short[]::new);

    }

    private void AddCellIfValid(int x, int y, List<Short> container) {
        if (!this.Map.pointMov(x, y, true, -1, -1)) {
            return;
        }
        /*if (!MapPoint.IsInMap(x, y)) {
            return;
        }*/
        container.add(MapPoint.CoordToCellId(x, y));
    }

    @Override
    public byte getMinRadius() {
        return _minRadius;
    }
    
     @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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
