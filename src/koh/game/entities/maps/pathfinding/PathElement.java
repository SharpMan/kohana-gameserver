package koh.game.entities.maps.pathfinding;

/**
 *
 * @author Neo-Craft
 */
public class PathElement {

    private MapPoint _oStep;
    private int _nOrientation;

    public PathElement() {
        this(null, 0);
    }

    public PathElement(MapPoint step) {
        this(step, 0);
    }

    public PathElement(MapPoint step, int orientation) {
        if (step == null) {
            this._oStep = new MapPoint();
        } else {
            this._oStep = step;
        };
        this._nOrientation = orientation;
    }

    public int get_orientation() {
        return (this._nOrientation);
    }

    public void set_orientation(int nValue) {
        this._nOrientation = nValue;
    }

    public MapPoint get_step() {
        return (this._oStep);
    }

    public void set_step(MapPoint nValue) {
        this._oStep = nValue;
    }

    public int get_cellId() {
        return (this._oStep.get_cellId());
    }

    @Override
    public String toString() {
        return ((((((((("[PathElement(cellId:" + this.get_cellId()) + ", x:") + this._oStep.get_x()) + ", y:") + this._oStep.get_y()) + ", orientation:") + this._nOrientation) + ")]"));
    }

}
