package koh.game.entities.environments;

import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class ObjectPosition {

    public int direction;
    public DofusCell cell;
    public DofusMap map;
    public MapPoint point;

    public ObjectPosition(ObjectPosition position) {
        this.map = position.map;
        this.cell = position.cell;
        this.direction = position.direction;
    }

    public ObjectPosition(DofusMap map, DofusCell cell) {
        this.map = map;
        this.cell = cell;
        this.direction = DirectionsEnum.RIGHT;
    }

    public ObjectPosition(DofusMap map, short cellId) {
        this.map = map;
        this.cell = map.getCell(cellId);
        this.direction = DirectionsEnum.RIGHT;
    }

    public ObjectPosition(DofusMap map, DofusCell cell, int direction) {
        this.map = map;
        this.cell = cell;
        this.direction = direction;
    }

    public ObjectPosition(DofusMap map, short cellId, int direction) {
        this.map = map;
        this.cell = map.getCell(cellId);
        this.direction = direction;
    }

    public ObjectPosition clone() {
        return new ObjectPosition(this.map, this.cell, this.direction);
    }

    public boolean isValid() {
        return this.cell.id > 0 && (long) this.cell.id < 560L && this.direction > DirectionsEnum.RIGHT && this.direction < DirectionsEnum.RIGHT && this.map != null;
    }

}
