package koh.game.entities.environments;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class ObjectPosition {

    public int Direction;
    public DofusCell Cell;
    public DofusMap Map;
    public MapPoint Point;

    public ObjectPosition(ObjectPosition position) {
        this.Map = position.Map;
        this.Cell = position.Cell;
        this.Direction = position.Direction;
    }

    public ObjectPosition(DofusMap map, DofusCell cell) {
        this.Map = map;
        this.Cell = cell;
        this.Direction = DirectionsEnum.RIGHT;
    }

    public ObjectPosition(DofusMap map, short cellId) {
        this.Map = map;
        this.Cell = map.getCell(cellId);
        this.Direction = DirectionsEnum.RIGHT;
    }

    public ObjectPosition(DofusMap map, DofusCell cell, int direction) {
        this.Map = map;
        this.Cell = cell;
        this.Direction = direction;
    }

    public ObjectPosition(DofusMap map, short cellId, int direction) {
        this.Map = map;
        this.Cell = map.getCell(cellId);
        this.Direction = direction;
    }

    public ObjectPosition Clone() {
        return new ObjectPosition(this.Map, this.Cell, this.Direction);
    }

    public boolean IsValid() {
        return this.Cell.id > 0 && (long) this.Cell.id < 560L && this.Direction > DirectionsEnum.RIGHT && this.Direction < DirectionsEnum.RIGHT && this.Map != null;
    }

}
