package koh.game.entities.environments;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import koh.game.entities.actors.IGameActor;
import koh.game.entities.actors.Player;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.protocol.client.enums.DirectionsEnum;
import lombok.Getter;
import org.apache.mina.core.buffer.IoBuffer;

/**
 *
 * @author Neo-Craft
 */
public class DofusCell {

    @Getter
    private  DofusMap map;
    @Getter
    private  short id;
    @Getter
    private  short floor;
    public int losMov = 3;
    @Getter
    private  byte speed;
    public int mapChangeData;
    @Getter
    private int moveZone;

    private final Map<Integer, IGameActor> myActors = Collections.synchronizedMap(new HashMap<>());
    public DofusTrigger myAction = null;

    private MapPoint point;

    public DofusCell(DofusMap map, short id, IoBuffer buf) {
        this.map = map;
        this.id = id;
        this.floor = buf.getShort();
        this.losMov = buf.getUnsigned();
        this.speed = buf.get();
        this.mapChangeData = buf.getUnsigned();
        this.moveZone = buf.getUnsigned();
    }

    public void addActor(IGameActor actor) {
        this.myActors.put(actor.getID(), actor);

        // on affecte la cell
        actor.setActorCell(this);

        if (actor instanceof Player && myAction != null) {
            ((Player) actor).getClient().setOnMouvementConfirm(myAction);
        }
    }

    public void delActor(IGameActor actor) {
        this.myActors.remove(actor.getID());
    }
    
    public Collection<IGameActor> getActors(){
        return this.myActors.values();
    }
    
    public boolean hasActor(){
        return !this.myActors.isEmpty();
    }

    public DofusCell(DofusMap map, short id, short Floor, byte LosMov, byte Speed, int MapChangeData, int MoveZone) {
        this.map = map;
        this.id = id;
        this.floor = Floor;
        this.losMov = LosMov;
        this.speed = Speed;
        this.mapChangeData = MapChangeData;
        this.moveZone = MoveZone;
    }

    public boolean affectMapChange() {
        return this.mapChangeData != 0;
    }

    public boolean los() {
        return ((this.losMov & 2) >> 1) == 1;
    }

    public boolean mov() {
        //return los() && !this.nonWalkableDuringFight() && !this.farmCell();
        return ((this.losMov & 1) == 1);
    }

    public boolean walakableInFight() {
        return ((this.losMov & 1) == 1) && !this.nonWalkableDuringFight() && !this.farmCell();
    }


    public boolean walakable() {
        return (this.losMov & 1) == 1;
    }

    public boolean nonWalkableDuringFight() {
        return (this.losMov & 4) >> 2 == 1;
    }

    public boolean nonWalkableDuringRP() {
        return (this.losMov & 128) >> 7 == 1;
    }

    public boolean farmCell() {
        return (this.losMov & 32) >> 5 == 1;
    }

    public boolean visible() {
        return (this.losMov & 64) >> 6 == 1;
    }

    public boolean red() {
        return (this.losMov & 8) >> 3 == 1;
    }

    public boolean blue() {
        return (this.losMov & 16) >> 4 == 1;
    }

    public MapPoint getPoint(){
        if(this.point == null)
            point = MapPoint.fromCellId(this.id);
        return this.point;
    }

    public int manhattanDistanceTo(DofusCell cell)
    {
        if (cell == null) return 255;
        return (Math.abs(getPoint().get_x() - cell.getPoint().get_x()) + Math.abs(getPoint().get_y() - cell.getPoint().get_y()));
    }

    public boolean isInRadius(DofusCell cell, int radius)
    {
        return manhattanDistanceTo(cell) <= radius;
    }

    public boolean isInRadius(DofusCell cell, int minRadius, int radius)
    {
        final int dist = manhattanDistanceTo(cell);
        return dist >= minRadius && dist <= radius;
    }

    public boolean isAdjacentTo(DofusCell cell){
        return isAdjacentTo(cell,true);
    }

    public boolean isAdjacentTo(DofusCell cell, boolean diagonal)
    {
        final int dist = diagonal ? distanceTo(cell) : manhattanDistanceTo(cell);

        return dist == 1;
    }

    public byte orientationTo(DofusCell cell) {
        return orientationTo(cell,true);
    }

    public byte orientationTo(DofusCell cell, boolean diagonal) {
        int dx = cell.getPoint().get_x() - getPoint().get_x();
        int dy = getPoint().get_y() - cell.getPoint().get_y();

        double distance = Math.sqrt(dx * dx + dy * dy);
        double angleInRadians = Math.acos(dx / distance);

        double angleInDegrees = angleInRadians * 180 / Math.PI;
        double transformedAngle = angleInDegrees * (cell.getPoint().get_y() > getPoint().get_y() ? (-1) : (1));

        double orientation = !diagonal ? Math.round(transformedAngle / 90) * 2 + 1 : Math.round(transformedAngle / 45) + 1;

        if (orientation < 0) {
            orientation = orientation + 8;
        }

        return  (byte) orientation;
    }

    public DofusCell getNearestCellInDirection(byte direction)
    {
        final MapPoint point = this.getPoint().getNearestCellInDirection(direction);
        if(point == null)
            return null;
        return this.map.getCell(point.get_cellId());
    }


    public boolean isChangeZone(DofusCell cell)
    {
        return moveZone != cell.moveZone && Math.abs(floor) == Math.abs(cell.floor);
    }


    public int distanceTo(DofusCell cell)
    {
        return (int)Math.sqrt((cell.getPoint().get_x() -getPoint().get_x()) * (cell.getPoint().get_x() - getPoint().get_x()) + (cell.getPoint().get_y() - getPoint().get_y()) * (cell.getPoint().get_y() - getPoint().get_y()));
    }


    public byte orientationToAdjacent(DofusCell cell) {
       Point vector = new Point( cell.getPoint().get_x() > getPoint().get_x() ? (1) : (cell.getPoint().get_x() < getPoint().get_x() ? (-1) : (0)),
               cell.getPoint().get_y() > getPoint().get_y() ? (1) : (cell.getPoint().get_y() < getPoint().get_y() ? (-1) : (0)));

        if (vector.equals(s_vectorRight))
        {
            return DirectionsEnum.RIGHT;
        }
        if (vector.equals(s_vectorDownRight))
        {
            return DirectionsEnum.DOWN_RIGHT;
        }
        if (vector.equals(s_vectorDown))
        {
            return DirectionsEnum.DOWN;
        }
        if (vector.equals(s_vectorDownLeft))
        {
            return DirectionsEnum.DOWN_LEFT;
        }
        if (vector.equals(s_vectorLeft))
        {
            return DirectionsEnum.LEFT;
        }
        if (vector.equals(s_vectorUpLeft))
        {
            return DirectionsEnum.UP_LEFT;
        }
        if (vector.equals(s_vectorUp))
        {
            return DirectionsEnum.UP;
        }
        if (vector.equals(s_vectorUpRight))
        {
            return DirectionsEnum.UP_RIGHT;
        }

        return DirectionsEnum.RIGHT;
    }

    private static final Point s_vectorRight = new Point(1, 1);
    private static final Point s_vectorDownRight = new Point(1, 0);
    private static final Point s_vectorDown = new Point(1, -1);
    private static final Point s_vectorDownLeft = new Point(0, -1);
    private static final Point s_vectorLeft = new Point(-1, -1);
    private static final Point s_vectorUpLeft = new Point(-1, 0);
    private static final Point s_vectorUp = new Point(-1, 1);
    private static final Point s_vectorUpRight = new Point(0, 1);

}
