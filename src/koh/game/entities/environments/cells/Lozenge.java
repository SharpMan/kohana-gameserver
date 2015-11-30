package koh.game.entities.environments.cells;

import java.util.ArrayList;
import java.util.List;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.maps.pathfinding.MapPoint;

/**
 *
 * @author Neo-Craft
 */
public class Lozenge implements IZone {

    public byte MinRadius;

    public byte Direction;

    public byte Radius;

    public DofusMap Map;

    public Lozenge(byte minRadius, byte radius,DofusMap Map) {
        this.MinRadius = minRadius;
        this.Radius = radius;
        this.Map = Map;
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
        return (((int) this.Radius + 1) * ((int) this.Radius + 1) + (int) this.Radius * (int) this.Radius);
    }

    @Override
    public Short[] getCells(short centerCell) {
        MapPoint mapPoint = MapPoint.fromCellId(centerCell);
        ArrayList<Short> list = new ArrayList<>();
        if (this.getRadius() == 0) {
            if (this.getMinRadius() == 0) {
                return new Short[]{centerCell};
            }
            return new Short[0];
        } else {
            int _loc8_ = 1;
            int _loc9_ = 0;
            int _loc6_ = 0;
            int _loc7_ = 0;
            int _loc4_ = mapPoint.get_x();
            int _loc5_ = mapPoint.get_y();
            _loc6_ = _loc4_ - this.getRadius();
            while (_loc6_ <= _loc4_ + this.getRadius()) {
                _loc7_ = -_loc9_;
                while (_loc7_ <= _loc9_) {
                    if (Math.abs(_loc4_ - _loc6_) + Math.abs(_loc7_) >= this.getMinRadius()) {
                        if (MapPoint.isInMap(_loc6_, _loc7_ + _loc5_)) {
                            addCell(_loc6_, _loc7_ + _loc5_, list);
                        }
                    }
                    _loc7_++;
                }
                if (_loc9_ == this.getRadius()) {
                    _loc8_ = -_loc8_;
                }
                _loc9_ = _loc9_ + _loc8_;
                _loc6_++;
            }
            return list.stream().toArray(Short[]::new);
        }
    }

    private void addCell(int x, int y, List<Short> container) {
        if (!this.Map.pointMov(x, y, true, -1, -1)) {
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

}
