package koh.game.entities.maps.pathfinding;

import java.util.ArrayList;
import java.util.List;
import koh.game.Main;
import koh.protocol.client.enums.DirectionsEnum;

/**
 *
 * @author Neo-Craft
 */
public class MovementPath {

    public static int MAX_PATH_LENGTH = 100;
    protected MapPoint _oStart;
    protected MapPoint _oEnd;
    protected List<PathElement> _aPath;

    public MovementPath() {
        this._oEnd = new MapPoint();
        this._oStart = new MapPoint();
        this._aPath = new ArrayList<>();
    }

    public MapPoint get_start() {
        return (this._oStart);
    }

    public void set_start(MapPoint nValue) {
        this._oStart = nValue;
    }

    public MapPoint get_end() {
        return (this._oEnd);
    }

    public void set_end(MapPoint nValue) {
        this._oEnd = nValue;
    }

    public List<PathElement> get_path() {
        return (this._aPath);
    }

    public void set_path(List<PathElement> value) {
        this._aPath = value;
    }

    public int get_length() {
        return (this._aPath.size());
    }

    public void fillFromCellIds(short[] cells) {
        int i = 0;
        while (i < cells.length) {
            this._aPath.add(new PathElement(MapPoint.fromCellId(cells[i])));
            i++;
        }
        i = 0;
        while (i < (cells.length - 1)) {
            this._aPath.get(i).set_orientation(this._aPath.get(i).get_step().orientationTo(this._aPath.get(i + 1).get_step()));
            i++;
        }
        if (!this._aPath.isEmpty()) {
            this._oStart = this._aPath.get(0).get_step();
            this._oEnd = this._aPath.get(this._aPath.size() - 1).get_step();
        }
    }

    public void addPoint(PathElement pathElem) {
        this._aPath.add(pathElem);
    }

    public PathElement getPointAtIndex(int index) {
        return this._aPath.get(index);
    }

    public void deletePoint(int index, int deleteCount) /*-1*/ {
        if (deleteCount <= 0) {
            this._aPath.remove(index);
            //this._aPath.splice(index);
        } else {
            int Index = index + deleteCount;
            while (Index >= index) {
                this._aPath.remove(Index);
                Index--;
            }

            //this._aPath.splice(index, deleteCount);
        }
    }

    public void compress() {
        int elem = 0;
        if (this._aPath.size() > 0) {
            elem = (this._aPath.size() - 1);
            while (elem > 0) {
                if (this._aPath.get(elem).get_orientation() == this._aPath.get((elem - 1)).get_orientation()) {
                    this.deletePoint(elem, -1);
                    elem--;
                } else {
                    elem--;
                }
            }
        }
    }

    public void fill() {
        int elem;
        PathElement pFinal;
        PathElement pe;
        if (this._aPath.size() > 0) {
            elem = 0;
            pFinal = new PathElement();
            pFinal.set_orientation(0);
            pFinal.set_step(this._oEnd);
            this._aPath.add(pFinal);
            while (elem < (this._aPath.size() - 1)) {
                if ((((Math.abs((this._aPath.get(elem).get_step().get_x() - this._aPath.get((elem + 1)).get_step().get_x())) > 1)) || ((Math.abs((this._aPath.get(elem).get_step().get_y() - this._aPath.get(elem + 1).get_step().get_y())) > 1)))) {
                    pe = new PathElement();
                    pe.set_orientation(this._aPath.get(elem).get_orientation());
                    switch (pe.get_orientation()) {
                        case DirectionsEnum.RIGHT:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() + 1), (this._aPath.get(elem).get_step().get_y() + 1)));
                            break;
                        case DirectionsEnum.DOWN_RIGHT:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() + 1), this._aPath.get(elem).get_step().get_y()));
                            break;
                        case DirectionsEnum.DOWN:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() + 1), (this._aPath.get(elem).get_step().get_y() - 1)));
                            break;
                        case DirectionsEnum.DOWN_LEFT:
                            pe.set_step(MapPoint.fromCoords(this._aPath.get(elem).get_step().get_x(), (this._aPath.get(elem).get_step().get_y() - 1)));
                            break;
                        case DirectionsEnum.LEFT:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() - 1), (this._aPath.get(elem).get_step().get_y() - 1)));
                            break;
                        case DirectionsEnum.UP_LEFT:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() - 1), this._aPath.get(elem).get_step().get_y()));
                            break;
                        case DirectionsEnum.UP:
                            pe.set_step(MapPoint.fromCoords((this._aPath.get(elem).get_step().get_x() - 1), (this._aPath.get(elem).get_step().get_y() + 1)));
                            break;
                        case DirectionsEnum.UP_RIGHT:
                            pe.set_step(MapPoint.fromCoords(this._aPath.get(elem).get_step().get_x(), (this._aPath.get(elem).get_step().get_y() + 1)));
                            break;
                    }
                    this._aPath.remove(elem + 1);
                    this._aPath.add(elem + 1, pe);
                    //this._aPath.splice((elem + 1), 0, pe);
                    elem++;
                } else {
                    elem++;
                }
                if (elem > MAX_PATH_LENGTH) {
                    Main.Logs().writeError("Path too long. Maybe an orientation problem?");
                }
            }
        }
        this._aPath.remove(this._aPath.size() - 1);
        //this._aPath.pop();
    }

    public short[] getCells() {
        MapPoint mp = null;
        short[] cells = new short[this._aPath.size() + 1];
        int i = 0;
        while (i < this._aPath.size()) {
            mp = this._aPath.get(i).get_step();
            cells[i] = (short) mp.get_cellId();
            i++;
        };
        cells[i] = (short) (this._oEnd.get_cellId());
        return (cells);
    }

    public void replaceEnd(MapPoint newEnd) {
        this._oEnd = newEnd;
    }

    @Override
    public String toString() {
        String str = (((("\ndepart : [" + this._oStart.get_x()) + ", ") + this._oStart.get_y()) + "]");
        str = (str + (((("\narrivée : [" + this._oEnd.get_x()) + ", ") + this._oEnd.get_y()) + "]\nchemin :"));
        int i = 0;
        while (i < this._aPath.size()) {
            str = (str + (((("[" + this._aPath.get(i).get_step().get_x() + ", ") + this._aPath.get(i).get_step().get_y()) + ", " + this._aPath.get(i).get_orientation()) + "] "));
            i++;
        }
        return (str);
    }

}
