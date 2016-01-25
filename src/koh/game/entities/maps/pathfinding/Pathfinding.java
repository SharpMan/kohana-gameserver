package koh.game.entities.maps.pathfinding;

import com.google.common.collect.Lists;
import koh.game.entities.environments.DofusMap;
import koh.game.fights.Fight;
import koh.game.fights.utils.CellInfo;
import koh.utils.Enumerable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Melancholia on 1/22/16.
 */
public class Pathfinding {

    private static int _minX = 0;
    private static int _maxX = 34;
    private static int _minY = -19;
    private static int _maxY = 14;

    private Map<Integer, Map<Integer, CellInfo>> _mapStatus;
    private List<int[]> _openList;
    private MovementPath _movPath;
    private int _nHVCost = 10;
    private int _nDCost = 15;
    private int _nHeuristicCost = 10;
    private boolean _bAllowDiagCornering = false;
    private boolean _bAllowTroughEntity;
    private boolean _bIsFighting;
    private boolean _enterFrameIsActive = false;
    private Fight fight;
    private DofusMap _map;
    private MapPoint _start;
    private MapPoint _end;
    private boolean _allowDiag;
    private int _endX;
    private int _endY;
    private MapPoint _endPoint;
    private MapPoint _startPoint;
    private int _startX;
    private int _startY;
    private MapPoint _endPointAux;
    private int _endAuxX;
    private int _endAuxY;
    private int _distanceToEnd;
    private int _nowY;
    private int _nowX;
    private int _currentTime;
    private int _maxTime = 30;
    private short _previousCellId;


    public static MovementPath findPath(Fight fight, MapPoint start, MapPoint end, boolean allowDiag, boolean bAllowTroughEntity) {
        return (new Pathfinding().processFindPath(fight, start, end, allowDiag, bAllowTroughEntity));
    }


    public MovementPath processFindPath(Fight fight, MapPoint start, MapPoint end, boolean allowDiag, boolean bAllowTroughEntity) {
        this._movPath = new MovementPath();
        this._movPath.set_start(start);
        this._movPath.set_end(end);
        this._bAllowTroughEntity = bAllowTroughEntity;
        this._map = fight.getMap();
        this.fight = fight;
        this._bIsFighting = /*this.fight != null*/true;
        this._bAllowDiagCornering = allowDiag;
        if (((start == null))) {
            return (this._movPath);
        }
        this.findPathInternal(_map, start, end, allowDiag);
        return (this._movPath);
    }

    private void findPathInternal(DofusMap map, MapPoint start, MapPoint end, boolean allowDiag) {
        int x;
        this._map = map;
        this._start = start;
        this._end = end;
        this._allowDiag = allowDiag;
        this._endPoint = MapPoint.fromCoords(end.get_x(), end.get_y());
        this._startPoint = MapPoint.fromCoords(start.get_x(), start.get_y());
        this._endX = end.get_x();
        this._endY = end.get_y();
        this._startX = start.get_x();
        this._startY = start.get_y();
        this._endPointAux = this._startPoint;
        this._endAuxX = this._startX;
        this._endAuxY = this._startY;
        this._distanceToEnd = this._startPoint.distanceToCell(this._endPoint);
        this._mapStatus = new HashMap<>();
        int y = _minY;
        while (y < _maxY) {
            this._mapStatus.put(y, new HashMap<>());
            x = _minX;
            while (x <= _maxX) {
                this._mapStatus.get(y).put(x, new CellInfo(0.0d, new int[0], false, false));
                x++;
            }
            y++;
        }
        this._openList = new ArrayList<>();
        this.openSquare(this._startY, this._startX, null, 0, null, false);
        this.initFindPath();
    }

    private void openSquare(int y, int x, int[] parent, int movementCost, Double heuristic, boolean replacing) {
        int len, i;
        if (!(replacing)) {
            //replacing = this._openList.stream()
            //       .anyMatch(ele -> ele[0] == y && ele[1] == x);

            len = this._openList.size();
            i = -1;
            while (++i < len) {
                if ((((this._openList.get(i)[0] == y)) && ((this._openList.get(i)[1] == x)))) {
                    replacing = true;
                    break;
                }
            }
        }
        if (!(replacing)) {
            this._openList.add(new int[]{y, x});
            this._mapStatus.get(y).put(x, new CellInfo(heuristic, null, true, false));
        }
        final CellInfo cellInfo = this._mapStatus.get(y).get(x);
        cellInfo.setParent(parent);
        cellInfo.setMovementCost(movementCost);
    }

    private MovementPath initFindPath() {
        return this.pathFrame(null);
        //TODO: Threadpool
        /*this._currentTime = 0;
        if (this._callBackFunction == null)
        {
            this._maxTime = 2000000;
            this.pathFrame(null);
        }
        else
        {
            if (!(this._enterFrameIsActive))
            {
                this._enterFrameIsActive = true;
                EnterFrameDispatcher.addEventListener(this.pathFrame, "pathFrame");
            };
            this._maxTime = 20;
        };*/
    }

    private MovementPath pathFrame(Object E/*Event*/) {
        int n, j, time, i;
        double pointWeight;
        int movementCost;
        boolean cellOnEndColumn;
        boolean cellOnStartColumn;
        boolean cellOnEndLine;
        boolean cellOnStartLine;
        MapPoint mp;
        int distanceTmpToEnd;
        double _local_14;
        /*if (this._currentTime == 0)
        {
            this._currentTime = getTimer();
        };*/
        if ((((this._openList.size() > 0)) && (!(this.isClosed(this._endY, this._endX))))) {
            n = this.nearerSquare();
            this._nowY = this._openList.get(n)[0];
            this._nowX = this._openList.get(n)[1];
            this._previousCellId = MapPoint.fromCoords(this._nowX, this._nowY).get_cellId();
            this.closeSquare(this._nowY, this._nowX);
            j = (this._nowY - 1);
            System.out.println(j );
            while (j < (this._nowY + 2)) {
                i = (this._nowX - 1);
                while (i < (this._nowX + 2)) {
                    if ((((((((((((j >= _minY)) && ((j < _maxY)))) && ((i >= _minX)))) && ((i < _maxX)))) && (!((((j == this._nowY)) && ((i == this._nowX))))))) && (((((this._allowDiag) || ((j == this._nowY)))) || ((((i == this._nowX)) && (((((((this._bAllowDiagCornering) || ((j == this._nowY)))) || ((i == this._nowX)))) || (((this.fight.pointMov(this._nowX, j, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId())) || (this.fight.pointMov(i, this._nowY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId())))))))))))) {
                        if (((((((!(this.fight.pointMov(this._nowX, j, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId()))) && (!(this.fight.pointMov(i, this._nowY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId()))))) && (!(this._bIsFighting)))) && (this._allowDiag))) {

                        } else {
                            if (this.fight.pointMov(i, j, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId())) {
                               if (!(this.isClosed(j, i))) {
                                    System.out.println("dk");
                                    if ((((i == this._endX)) && ((j == this._endY)))) {
                                        pointWeight = 1;
                                    } else {
                                        pointWeight = this.fight.pointWeight(i, j, this._bAllowTroughEntity);
                                    }

                                    movementCost = (int) (this._mapStatus.get(this._nowY).get(this._nowX).getMovementCost() + ((((((j == this._nowY)) || ((i == this._nowX)))) ? this._nHVCost : this._nDCost) * pointWeight));
                                    System.out.println("c"+movementCost +" "+pointWeight);
                                    if (this._bAllowTroughEntity) {
                                        cellOnEndColumn = ((i + j) == (this._endX + this._endY));
                                        cellOnStartColumn = ((i + j) == (this._startX + this._startY));
                                        cellOnEndLine = ((i - j) == (this._endX - this._endY));
                                        cellOnStartLine = ((i - j) == (this._startX - this._startY));
                                        mp = MapPoint.fromCoords(i, j);
                                        if (((((!(cellOnEndColumn)) && (!(cellOnEndLine)))) || (((!(cellOnStartColumn)) && (!(cellOnStartLine)))))) {
                                            movementCost = (movementCost + mp.distanceToCell(this._endPoint));
                                            movementCost = (movementCost + mp.distanceToCell(this._startPoint));
                                        }
                                        if ((((i == this._endX)) || ((j == this._endY)))) {
                                            movementCost = (movementCost - 3);
                                        }
                                        if (((((((cellOnEndColumn) || (cellOnEndLine))) || (((i + j) == (this._nowX + this._nowY))))) || (((i - j) == (this._nowX - this._nowY))))) {
                                            movementCost = (movementCost - 2);
                                        }
                                        if ((((i == this._startX)) || ((j == this._startY)))) {
                                            movementCost = (movementCost - 3);
                                        }
                                        if (((cellOnStartColumn) || (cellOnStartLine))) {
                                            movementCost = (movementCost - 2);
                                        }
                                        distanceTmpToEnd = mp.distanceToCell(this._endPoint);
                                        if (distanceTmpToEnd < this._distanceToEnd) {
                                            if ((((((((i == this._endX)) || ((j == this._endY)))) || (((i + j) == (this._endX + this._endY))))) || (((i - j) == (this._endX - this._endY))))) {
                                                this._endPointAux = mp;
                                                this._endAuxX = i;
                                                this._endAuxY = j;
                                                this._distanceToEnd = distanceTmpToEnd;
                                            }
                                        }
                                    }
                                    if (this.isOpened(j, i)) {
                                        if (movementCost < this._mapStatus.get(j).get(i).getMovementCost()) {
                                            this.openSquare(j, i, new int[]{this._nowY, this._nowX}, movementCost, null, true);
                                        }
                                    } else {
                                        _local_14 = (this._nHeuristicCost * Math.sqrt((((this._endY - j) * (this._endY - j)) + ((this._endX - i) * (this._endX - i)))));
                                        this.openSquare(j, i, new int[]{this._nowY, this._nowX}, movementCost, _local_14, false);
                                    }
                                }
                            }
                        }
                    }
                    i++;
                }
                j++;
            }
            /*time = getTimer();
            if ((time - this._currentTime) < this._maxTime)
            {
                this.pathFrame(null);
            }
            else
            {
                this._currentTime = 0;
            };*/
            this._currentTime = 0;
        }
        return this.endPathFrame();

    }


    private boolean isOpened(int y, int x) {
        return (this._mapStatus.get(y).get(x).isOpened());
    }

    private boolean isClosed(int y, int x) {
        final CellInfo cellInfo = this._mapStatus.get(y).get(x);
        //return cellInfo != null && cellInfo.isClosed();
        if ((((cellInfo == null)))) {
            return (false);
        }

        return (cellInfo.isClosed());
    }


    private int nearerSquare() {
        double thisF;
        double minimum = 9999999;
        int indexFound = 0;
        int i = -1;
        final int len = this._openList.size();
        while (++i < len) {
            thisF = (this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).getHeuristic() + this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).getMovementCost());
            thisF = (this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).getHeuristic() + this._mapStatus.get(this._openList.get(i)[0]).get(this._openList.get(i)[1]).getMovementCost());
            if (thisF <= minimum) {
                minimum = thisF;
                indexFound = i;
            }
        }
        return (indexFound);
    }

    private void closeSquare(int y, int x) {
        final int len = this._openList.size();
        int i = -1;
        while (++i < len) {
            if (this._openList.get(i)[0] == y) {
                if (this._openList.get(i)[1] == x) {
                    this._openList.remove(i);
                    //FIXME: not sure this._openList.splice(i, 1);
                    break;
                }
            }
        }
        CellInfo cellInfo = this._mapStatus.get(y).get(x);
        cellInfo.setOpened(false);
        cellInfo.setClosed(true);
    }

    private MovementPath endPathFrame()
    {
        List<MapPoint> returnPath;
        int newY,newX;
        MapPoint tmpMapPoint;
        List<MapPoint>  returnPathOpti;
        int k,kX,kY,nextX,nextY,interX,interY;
        this._enterFrameIsActive = false;
        //EnterFrameDispatcher.removeEventListener(this.pathFrame);
        boolean pFound = this.isClosed(this._endY, this._endX);
        if (!(pFound))
        {
            this._endY = this._endAuxY;
            this._endX = this._endAuxX;
            this._endPoint = this._endPointAux;
            pFound = true;
            this._movPath.replaceEnd(this._endPoint);
        }
        this._previousCellId = -1;
        if (pFound)
        {
            returnPath = new ArrayList<>();
            this._nowY = this._endY;
            this._nowX = this._endX;
            while (((!((this._nowY == this._startY))) || (!((this._nowX == this._startX)))))
            {
                returnPath.add(MapPoint.fromCoords(this._nowX, this._nowY));
                newY = this._mapStatus.get(this._nowY).get(this._nowX).getParent()[0];
                newX = this._mapStatus.get(this._nowY).get(this._nowX).getParent()[1];
                System.out.println( newY+" "+ newX);
                this._nowY = newY;
                this._nowX = newX;
            }
            returnPath.add(this._startPoint);
            if (this._allowDiag)
            {
                returnPathOpti = new ArrayList<>();
                k = 0;
                while (k < returnPath.size())
                {
                    returnPathOpti.add(returnPath.get(k));
                    this._previousCellId = returnPath.get(k).get_cellId();
                    if ((((((k + 2) < returnPath.size() ) && ((returnPath.get(k).distanceToCell(returnPath.get(k +2)) == 1)) && (!(this._map.isChangeZone(returnPath.get(k).get_cellId(), returnPath.get(k + 1).get_cellId()))))) && (!(this._map.isChangeZone(returnPath.get(k + 1).get_cellId(), returnPath.get(k + 2).get_cellId())))))
                    {
                        k++;
                    }
                    else
                    {
                        if (((((k+3) < returnPath.size()) && returnPath.get(k).distanceToCell(returnPath.get(k + 3)) == 2)))
                        {
                            kX = returnPath.get(k).get_x();
                            kY = returnPath.get(k).get_y();
                            nextX = returnPath.get(k + 3).get_x();
                            nextY = returnPath.get(k + 3).get_y();
                            interX = (kX + Math.round(((nextX - kX) / 2)));
                            interY = (kY + Math.round(((nextY - kY) / 2)));
                            if (((this.fight.pointMov(interX, interY, true, this._previousCellId, this._endPoint.get_cellId())) && ((this.fight.pointWeight(interX, interY) < 2))))
                            {
                                tmpMapPoint = MapPoint.fromCoords(interX, interY);
                                returnPathOpti.add(tmpMapPoint);
                                this._previousCellId = tmpMapPoint.get_cellId();
                                k++;
                                k++;
                            }
                        }
                        else
                        {
                            if (((((k + 2) < returnPath.size() )) && (returnPath.get(k).distanceToCell(returnPath.get(k + 2)) == 2)))
                            {
                                kX = returnPath.get(k).get_x();
                                kY = returnPath.get(k).get_y();
                                nextX = returnPath.get(k + 2).get_x();
                                nextY = returnPath.get(k + 2).get_y();
                                interX = returnPath.get(k +1).get_x();
                                interY = returnPath.get(k + 1).get_y();
                                if (((((((((kX + kY) == (nextX + nextY))) && (!(((kX - kY) == (interX - interY)))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(kX, kY).get_cellId(), MapPoint.fromCoords(interX, interY).get_cellId()))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(interX, interY).get_cellId(), MapPoint.fromCoords(nextX, nextY).get_cellId())))))
                                {
                                    k++;
                                }
                                else
                                {
                                    if (((((((((kX - kY) == (nextX - nextY))) && (!(((kX - kY) == (interX - interY)))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(kX, kY).get_cellId(), MapPoint.fromCoords(interX, interY).get_cellId()))))) && (!(this._map.isChangeZone(MapPoint.fromCoords(interX, interY).get_cellId(), MapPoint.fromCoords(nextX, nextY).get_cellId())))))
                                    {
                                        k++;
                                    }
                                    else
                                    {
                                        if ((((((((kX == nextX)) && (!((kX == interX))))) && ((this.fight.pointWeight(kX, interY) < 2)))) && (this.fight.pointMov(kX, interY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId()))))
                                        {
                                            tmpMapPoint = MapPoint.fromCoords(kX, interY);
                                            returnPathOpti.add(tmpMapPoint);
                                            this._previousCellId = tmpMapPoint.get_cellId();
                                            k++;
                                        }
                                        else
                                        {
                                            if ((((((((kY == nextY)) && (!((kY == interY))))) && ((this.fight.pointWeight(interX, kY) < 2)))) && (this.fight.pointMov(interX, kY, this._bAllowTroughEntity, this._previousCellId, this._endPoint.get_cellId()))))
                                            {
                                                tmpMapPoint = MapPoint.fromCoords(interX, kY);
                                                returnPathOpti.add(tmpMapPoint);
                                                this._previousCellId = tmpMapPoint.get_cellId();
                                                k++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    k++;
                }
                returnPath = returnPathOpti;
            }
            System.out.println(returnPath.size());
            if (returnPath.size() == 1)
            {
                returnPath = new ArrayList<>();
            }
            returnPath = Lists.reverse(returnPath);
            return movementPathFromArray(returnPath);
        }
        return _movPath;
        /*if (this._callBackFunction != null)
        {
            if (this._argsFunction)
            {
                this._callBackFunction(this._movPath, this._argsFunction);
            }
            else
            {
                this._callBackFunction(this._movPath);
            };
        };*/
    }

    private MovementPath movementPathFromArray(List<MapPoint> returnPath)
    {
        PathElement pElem;
        int i = 0;
        System.out.println("c"+returnPath.size());
        while (i < (returnPath.size() - 1))
        {
            pElem = new PathElement();
            pElem.get_step().set_x(returnPath.get(i).get_x());
            pElem.get_step().set_y(returnPath.get(i).get_y());
            pElem.set_orientation(returnPath.get(i).orientationTo(returnPath.get(i +1)));
            this._movPath.addPoint(pElem);
            i++;
        };
        this._movPath.compress();
        this._movPath.fill();
        return _movPath;
    }

}




