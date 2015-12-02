package koh.game.entities.maps.pathfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import koh.game.Main;
import koh.game.entities.environments.DofusMap;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class MapPoint {

    private static final Point VECTOR_RIGHT = new Point(1, 1);
    private static final Point VECTOR_DOWN_RIGHT = new Point(1, 0);
    private static final Point VECTOR_DOWN = new Point(1, -1);
    private static final Point VECTOR_DOWN_LEFT = new Point(0, -1);
    private static final Point VECTOR_LEFT = new Point(-1, -1);
    private static final Point VECTOR_UP_LEFT = new Point(-1, 0);
    private static final Point VECTOR_UP = new Point(-1, 1);
    private static final Point VECTOR_UP_RIGHT = new Point(0, 1);

    public static final int MAP_WIDTH = 14;
    public static final int MAP_HEIGHT = 20;
    private static boolean _bInit = false;
    public static Point[] CELLPOS = new Point[MAP_HEIGHT * (MAP_WIDTH * 2)];

    static {
        init();
    }

    public static boolean IsInMap(int x, int y) {
        return x + y >= 0 && x - y >= 0 && (long) (x - y) < 40L && (long) (x + y) < 28L;
    }

    public static short coordToCellId(int x, int y) {
        if (!(_bInit)) {
            init();
        }
        return (short)(((((x - y) * MAP_WIDTH) + y) + ((x - y) / 2)));
    }

    private int _nCellId;
    private int _nX;
    private int _nY;

    public static MapPoint fromCellId(int cellId) {
        MapPoint mp = new MapPoint();
        mp._nCellId = cellId;
        mp.setFromCellId();
        return (mp);
    }

    public static MapPoint fromCoords(int x, int y) {
        MapPoint mp = new MapPoint();
        mp._nX = x;
        mp._nY = y;
        mp.setFromCoords();
        return (mp);
    }

    public static int getOrientationsDistance(int currentOrientation, int defaultOrientation) {
        return (Math.min(Math.abs((defaultOrientation - currentOrientation)), Math.abs(((8 - defaultOrientation) + currentOrientation))));
    }

    public static boolean isInMap(int x, int y) {
        return (((((((((x + y) >= 0)) && (((x - y) >= 0)))) && (((x - y) < (MAP_HEIGHT * 2))))) && (((x + y) < (MAP_WIDTH * 2)))));
    }

    private static void init() {
        int b;
        _bInit = true;
        int startX = 0;
        int startY = 0;
        int cell = 0;
        int a = 0;
        while (a < MAP_HEIGHT) {
            b = 0;
            while (b < MAP_WIDTH) {
                CELLPOS[cell] = new Point((startX + b), (startY + b));
                cell++;
                b++;
            }
            startX++;
            b = 0;
            while (b < MAP_WIDTH) {
                CELLPOS[cell] = new Point((startX + b), (startY + b));
                cell++;
                b++;
            }
            startY--;
            a++;
        }
    }

    public short get_cellId() {
        return (short) (this._nCellId);
    }

    public void set_cellId(int nValue) {
        this._nCellId = nValue;
        this.setFromCellId();
    }

    public int get_x() {
        return (this._nX);
    }

    public void set_x(int nValue) {
        this._nX = nValue;
        this.setFromCoords();
    }

    public int get_y() {
        return (this._nY);
    }

    public void set_y(int nValue) {
        this._nY = nValue;
        this.setFromCoords();
    }

    public Point get_coordinates() {
        return (new Point(this._nX, this._nY));
    }

    public int distanceTo(MapPoint mp) {
        return (int) (Math.sqrt((Math.pow((mp.get_x() - this.get_x()), 2) + Math.pow((mp.get_y() - this.get_y()), 2))));
    }

    public int distanceToCell(MapPoint cell) {
        return ((Math.abs((this.get_x() - cell.get_x())) + Math.abs((this.get_y() - cell.get_y()))));
    }

    public byte orientationTo(MapPoint mp) {
        byte result = -1;
        if ((((this.get_x() == mp.get_x())) && ((this.get_y() == mp.get_y())))) {
            return (1);
        }
        Point pt = new Point();

        pt.x = (((mp.get_x()) > this.get_x()) ? 1 : (((mp.get_x()) < this.get_x()) ? -1 : 0));
        pt.y = (((mp.get_y()) > this.get_y()) ? 1 : (((mp.get_y()) < this.get_y()) ? -1 : 0));

        if ((((pt.x == VECTOR_RIGHT.x)) && ((pt.y == VECTOR_RIGHT.y)))) {
            result = DirectionsEnum.RIGHT;
        } else {
            if ((((pt.x == VECTOR_DOWN_RIGHT.x)) && ((pt.y == VECTOR_DOWN_RIGHT.y)))) {
                result = DirectionsEnum.DOWN_RIGHT;
            } else {
                if ((((pt.x == VECTOR_DOWN.x)) && ((pt.y == VECTOR_DOWN.y)))) {
                    result = DirectionsEnum.DOWN;
                } else {
                    if ((((pt.x == VECTOR_DOWN_LEFT.x)) && ((pt.y == VECTOR_DOWN_LEFT.y)))) {
                        result = DirectionsEnum.DOWN_LEFT;
                    } else {
                        if ((((pt.x == VECTOR_LEFT.x)) && ((pt.y == VECTOR_LEFT.y)))) {
                            result = DirectionsEnum.LEFT;
                        } else {
                            if ((((pt.x == VECTOR_UP_LEFT.x)) && ((pt.y == VECTOR_UP_LEFT.y)))) {
                                result = DirectionsEnum.UP_LEFT;
                            } else {
                                if ((((pt.x == VECTOR_UP.x)) && ((pt.y == VECTOR_UP.y)))) {
                                    result = DirectionsEnum.UP;
                                } else {
                                    if ((((pt.x == VECTOR_UP_RIGHT.x)) && ((pt.y == VECTOR_UP_RIGHT.y)))) {
                                        result = DirectionsEnum.UP_RIGHT;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (result);
    }

    public int advancedOrientationTo(MapPoint mp) {
        return advancedOrientationTo(mp, true);
    }

    public byte advancedOrientationTo(MapPoint mp, boolean fourDir) {
        if (mp == null) {
            return (0);
        }
        int ac = (mp.get_x() - this.get_x());
        int bc = (this.get_y() - mp.get_y());
        int angle = (int) (((Math.acos((ac / Math.sqrt((Math.pow(ac, 2) + Math.pow(bc, 2))))) * 180) / Math.PI) * (((mp.get_y() > this.get_y())) ? -1 : 1));
        if (fourDir) {
            angle = ((Math.round((angle / 90)) * 2) + 1);
        } else {
            angle = (Math.round((angle / 45)) + 1);
        }
        if (angle < 0) {
            angle = (angle + 8);
        }
        return (byte) (angle);
    }

    public MapPoint getNearestFreeCell(DofusMap mapProvider) {
        return getNearestFreeCell(mapProvider, true);
    }

    public MapPoint getNearestFreeCell(DofusMap mapProvider, Boolean allowThoughEntity) {
        MapPoint mp = null;
        int i = 0;
        while (i < 8) {
            mp = this.getNearestFreeCellInDirection(i, mapProvider, false, allowThoughEntity);
            if (mp != null) {//ToCheck
                break;
            }
            i++;
        }
        return (mp);
    }

    public MapPoint getNearestCellInDirection(int orientation) {
        MapPoint mp = null;
        switch (orientation) {
            case 0:
                mp = MapPoint.fromCoords((this._nX + 1), (this._nY + 1));
                break;
            case 1:
                mp = MapPoint.fromCoords((this._nX + 1), this._nY);
                break;
            case 2:
                mp = MapPoint.fromCoords((this._nX + 1), (this._nY - 1));
                break;
            case 3:
                mp = MapPoint.fromCoords(this._nX, (this._nY - 1));
                break;
            case 4:
                mp = MapPoint.fromCoords((this._nX - 1), (this._nY - 1));
                break;
            case 5:
                mp = MapPoint.fromCoords((this._nX - 1), this._nY);
                break;
            case 6:
                mp = MapPoint.fromCoords((this._nX - 1), (this._nY + 1));
                break;
            case 7:
                mp = MapPoint.fromCoords(this._nX, (this._nY + 1));
                break;
        }
        if (MapPoint.isInMap(mp._nX, mp._nY)) {
            return (mp);
        }
        return (null);
    }

    public MapPoint getNearestFreeCellInDirection(int orientation, DofusMap mapProvider, boolean allowItself) {
        return getNearestFreeCellInDirection(orientation, mapProvider, allowItself, true, false, null);

    }

    public MapPoint getNearestFreeCellInDirection(int orientation, DofusMap mapProvider, boolean allowItself, boolean allowThoughEntity) {
        return getNearestFreeCellInDirection(orientation, mapProvider, allowItself, allowThoughEntity, false, null);

    }

    public MapPoint getNearestFreeCellInDirection(int orientation, DofusMap mapProvider, boolean allowItself, boolean allowThoughEntity, boolean ignoreSpeed) {
        return getNearestFreeCellInDirection(orientation, mapProvider, allowItself, allowThoughEntity, ignoreSpeed, null);

    }

    public MapPoint getNearestFreeCellInDirection(int orientation, DofusMap mapProvider, boolean allowItself, boolean allowThoughEntity, boolean ignoreSpeed, List<Short> forbidenCellsId) {
        int i = 0;
        int speed = 0;
        int weight = 0;
        MapPoint mp = null;
        if (forbidenCellsId == null) {
            forbidenCellsId = new ArrayList();
        }
        MapPoint[] cells = new MapPoint[8];
        int[] weights = new int[8];
        i = 0;
        while (i < 8) {
            mp = this.getNearestCellInDirection(i);
            if (((!((mp == null))) && ((forbidenCellsId.indexOf(mp.get_cellId()) == -1)))) {
                speed = mapProvider.getCellSpeed(mp.get_cellId());
                if (!(mapProvider.pointMov(mp._nX, mp._nY, allowThoughEntity, this.get_cellId(), -1))) {
                    speed = -100;
                }
                weights[i] = (getOrientationsDistance(i, orientation) + ((!(ignoreSpeed)) ? (((speed) >= 0) ? (5 - speed) : (11 + Math.abs(speed))) : 0));
            } else {
                weights[i] = 1000;
            }
            cells[i] = mp;
            i++;
        }
        mp = null;
        int minWeightOrientation = 0;
        int minWeight = weights[0];
        i = 1;
        while (i < 8) {
            weight = weights[i];
            if ((((weight < minWeight)) && (!((cells[i] == null))))) {
                minWeight = weight;
                minWeightOrientation = i;
            }
            i++;
        }
        mp = cells[minWeightOrientation];
        if ((((((mp == null)) && (allowItself))) && (mapProvider.pointMov(this._nX, this._nY, allowThoughEntity, this.get_cellId(), -1)))) {
            return (this);
        }
        return (mp);
    }

    public MapPoint pointSymetry(MapPoint pCentralPoint) {
        int destX = ((2 * pCentralPoint.get_x()) - this.get_x());
        int destY = ((2 * pCentralPoint.get_y()) - this.get_y());
        if (isInMap(destX, destY)) {
            return (MapPoint.fromCoords(destX, destY));
        }
        return (null);
    }

    @Override
    public boolean equals(Object o) {
        return ((((MapPoint) o).get_cellId() == this.get_cellId()));
    }

    @Override
    public String toString() {
        return ((((((("[getMapPoint(x:" + this._nX) + ", y:") + this._nY) + ", id:") + this._nCellId) + ")]"));
    }

    private void setFromCoords() {
        if (!(_bInit)) {
            init();
        }
        this._nCellId = ((((this._nX - this._nY) * MAP_WIDTH) + this._nY) + ((this._nX - this._nY) / 2));
    }

    private void setFromCellId() {
        if (!(_bInit)) {
            init();
        }
        if (CELLPOS[this._nCellId] == null) {
            Main.Logs().writeError((("cell identifier out of bounds (" + this._nCellId) + ")."));
        }
        Point p = CELLPOS[this._nCellId];
        this._nX = p.x;
        this._nY = p.y;
    }

    public static int getx(int cell) {
        if (!(_bInit)) {
            init();
        }
        return CELLPOS[cell].x;
    }

    public static int GetY(int cell) {
        if (!(_bInit)) {
            init();
        }
        return CELLPOS[cell].y;
    }

   public Point coordinates() {
        return new Point(this._nX,this._nY);
    }

}
