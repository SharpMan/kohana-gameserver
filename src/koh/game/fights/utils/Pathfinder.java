package koh.game.fights.utils;

import com.google.common.collect.Lists;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.maps.pathfinding.MapPoint;
import koh.game.entities.maps.pathfinding.MapTools;
import koh.game.fights.Fight;
import koh.protocol.client.enums.DirectionsEnum;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Melancholia on 1/26/16.
 */
public class Pathfinder {
    public class PathNode
    {
        public DofusCell cell;

        public double F()
        {
            return heuristic + cost;
        }

        public double cost;
        public double heuristic;
        public DofusCell parent;
        public NodeState status;
    }

    public enum NodeState
    {
        NONE,
        OPEN,
        CLOSED
    }

    private final DofusMap map;
    private final Fight context;
    private final boolean throughEntities;
    private final boolean useLogNodeSearch;
    public static final int ESTIMATE_HEURISTIC = 10;
    public static final int DIAGONAL_COST = 15;
    public static final int HORIZONTAL_COST = 10;
    public static final int SEARCH_LIMIT = 330;

    private static final byte[] DIRECTIONS = new byte[]
    {
            DirectionsEnum.LEFT,
            DirectionsEnum.UP_LEFT,
            DirectionsEnum.UP,
            DirectionsEnum.DOWN_LEFT,
            DirectionsEnum.UP_RIGHT,
            DirectionsEnum.DOWN,
            DirectionsEnum.DOWN_RIGHT,
            DirectionsEnum.RIGHT,

    };

    private static final byte[] DIAGONALS_DIRECTIONS = new byte[]
            {
                    0, 2, 5, 7
            };

    private final static byte[] ADJACENTS = new byte[] {
            DirectionsEnum.DOWN_LEFT,
            DirectionsEnum.DOWN_RIGHT,
            DirectionsEnum.UP_LEFT,
            DirectionsEnum.UP_RIGHT
    };



    private static double GetHeuristic(DofusCell pointA, DofusCell pointB)
    {
        return ESTIMATE_HEURISTIC * pointA.manhattanDistanceTo(pointB);
    }

    public Pathfinder(DofusMap map, Fight context, boolean throughEntities, boolean useLogNodeSearch/*= false*/)
    {
        this.map = map;
        this.context = context;
        this.throughEntities = throughEntities;
        // the dofus client use a bad linear algorithm to find the closest node.
        // if we use an other sort method the result may be different
        this.useLogNodeSearch = useLogNodeSearch;
    }

    public final Path findPath(DofusCell startCell, DofusCell endCell, boolean allowDiagonals, int movementPoints /*= (short)-1*/)
    {
        boolean success = false;

        PathNode[] matrix = new PathNode[MapTools._CELLCOUNT + 1];
        for(int i = 0; i < matrix.length; ++i){
            matrix[i] = new PathNode();
            matrix[i].status = NodeState.NONE;
        }
        IOpenList openList = /*useLogNodeSearch ? (IOpenList)new LogOpenList(new ComparePfNodeMatrix(matrix)) :*/
                new LinearOpenList(new ComparePfNodeMatrix(matrix));
        List<PathNode> closedList = new ArrayList<>();

        DofusCell location = startCell;
        int counter = 0;

        //if (movementPoints == 0)
        //    return Path.getEmptyPath(map, startCell);

        matrix[location.getId()].cell = location;
        matrix[location.getId()].parent = null;
        matrix[location.getId()].cost = 0;
        matrix[location.getId()].heuristic = 0;
        matrix[location.getId()].status = NodeState.OPEN;

        int distToEnd = startCell.manhattanDistanceTo(endCell);
        DofusCell endCellAux = startCell;

        openList.push(location);
        while (openList.getCount() > 0)
        {
            location = openList.pop();
            matrix[location.getId()].status = NodeState.CLOSED;

            if (location == endCell)
            {
                success = true;
                break;
            }

            if (counter > SEARCH_LIMIT)
                return Path.getEmptyPath(context, startCell);

            for (byte i = 0; i < 8; i++)
            {

                final boolean isDiagonal = ArrayUtils.contains(DIAGONALS_DIRECTIONS,i);



                if (isDiagonal && !allowDiagonals)
                    continue;


                DofusCell newLocation = location.getNearestCellInDirection(DIRECTIONS[i]);

                if (newLocation == null)
                    continue;

                if (newLocation.getId() < 0 || newLocation.getId() >= MapTools._CELLCOUNT)
                    continue;

                if (matrix[newLocation.getId()].status == NodeState.CLOSED)
                    continue;

                if (!context.getCell(newLocation.getId()).canWalk())
                    continue;

                double baseCost;

                if (newLocation == endCell)
                    baseCost = 1;
                else
                    baseCost = getCellCost(newLocation, throughEntities);

                double cost = matrix[location.getId()].cost + baseCost * ( isDiagonal ? DIAGONAL_COST : HORIZONTAL_COST);

                // adjust the cost if the current cell is aligned with the start cell or the end cell
                if (throughEntities)
                {
                    boolean alignedWithEnd = newLocation.getPoint().get_x() + newLocation.getPoint().get_y() == endCell.getPoint().get_x() + endCell.getPoint().get_y() ||
                            newLocation.getPoint().get_x() - newLocation.getPoint().get_y() == endCell.getPoint().get_x()- endCell.getPoint().get_y();
                    boolean alignedWithStart = newLocation.getPoint().get_x() + newLocation.getPoint().get_y() == startCell.getPoint().get_x() + startCell.getPoint().get_y() ||
                            newLocation.getPoint().get_x() - newLocation.getPoint().get_y() == startCell.getPoint().get_x() - startCell.getPoint().get_y();

                    if (newLocation.getPoint().get_x() + newLocation.getPoint().get_y() != endCell.getPoint().get_x() + endCell.getPoint().get_y() && newLocation.getPoint().get_x() - newLocation.getPoint().get_y() != endCell.getPoint().get_x() - endCell.getPoint().get_y() ||
                            newLocation.getPoint().get_x() + newLocation.getPoint().get_y() != startCell.getPoint().get_x() + startCell.getPoint().get_y() && newLocation.getPoint().get_x() - newLocation.getPoint().get_y() != startCell.getPoint().get_x() - startCell.getPoint().get_y())
                    {
                        cost += newLocation.manhattanDistanceTo(endCell);
                        cost += newLocation.manhattanDistanceTo(startCell);
                    }

                    // tests diagonales now
                    if (newLocation.getPoint().get_x() == endCell.getPoint().get_x() || newLocation.getPoint().get_y() == endCell.getPoint().get_y())
                        cost -= 3;

                    if (alignedWithEnd || !isDiagonal)
                        cost -= 2;

                    if (newLocation.getPoint().get_x() == startCell.getPoint().get_x() || newLocation.getPoint().get_y() == startCell.getPoint().get_y())
                        cost -= 3;

                    if (alignedWithStart)
                        cost -= 2;

                    final int currentDistToEnd = newLocation.manhattanDistanceTo(endCell);

                    if (currentDistToEnd < distToEnd)
                    {
                        // if aligned with end
                        if (newLocation.getPoint().get_x() == endCell.getPoint().get_x() || newLocation.getPoint().get_y() == endCell.getPoint().get_y() ||
                                alignedWithEnd)
                        {
                            distToEnd = currentDistToEnd;
                            endCellAux = newLocation;
                        }
                    }
                }

                if (matrix[newLocation.getId()].status == NodeState.OPEN)
                {
                    if (matrix[newLocation.getId()].cost <= cost)
                        continue;

                    matrix[newLocation.getId()].parent = location;
                    matrix[newLocation.getId()].cost = cost;
                }
                else
                {
                    matrix[newLocation.getId()].cell = newLocation;
                    matrix[newLocation.getId()].parent = location;
                    matrix[newLocation.getId()].cost = cost;
                    matrix[newLocation.getId()].heuristic = GetHeuristic(newLocation, endCell);

                    openList.push(newLocation);
                }

                matrix[newLocation.getId()].status = NodeState.OPEN;
            }

            counter++;
        }


        if (success)
        {
            PathNode node = matrix[endCell.getId()];

            // use auxiliary end if not found
            if (node.status != NodeState.CLOSED)
                node = matrix[endCellAux.getId()];

            while (node.parent != null)
            {
                closedList.add(node);
                node = matrix[node.parent.getId()];
            }

            closedList.add(node);
        }

        closedList = Lists.reverse(closedList);

        if (allowDiagonals)
            return createAndOptimisePath(closedList);
        else
        {
            if (movementPoints > 0 && closedList.size()  > movementPoints + 1)
                return new Path(context, closedList.subList(0, movementPoints + 1).stream().map(entry -> entry.cell).toArray(DofusCell[]::new));


            return new Path(context, closedList.stream().map(entry -> entry.cell).toArray(DofusCell[]::new));
        }
    }

    private Path createAndOptimisePath(List<PathNode> nodes)
    {
        final List<DofusCell> cells = new ArrayList<>();
        final int len = nodes.size();

        for (int i = 0; i < len; i++)
        {
            PathNode node = nodes.get(i);
            DofusCell cell = node.cell;

            cells.add(cell);

            if (i + 2 < len && cell.manhattanDistanceTo(nodes.get(i+2).cell) == 1 &&
                    !cell.isChangeZone(nodes.get(i+1).cell) &&
                    !nodes.get(i+1).cell.isChangeZone(nodes.get(i+2).cell))
            {
                i++;
            }

            else if (i + 3 < len && cell.manhattanDistanceTo(nodes.get(i+3).cell) == 2)
            {
                MapPoint middle = MapPoint.fromCoords(cell.getPoint().get_x() + (int) Math.round((nodes.get(i +3).cell.getPoint().get_x() - cell.getPoint().get_x()) / 2d),
                        cell.getPoint().get_y() + (int) Math.round((nodes.get(i+3).cell.getPoint().get_y() - cell.getPoint().get_y()) / 2d));

                final DofusCell middleCell = map.getCell(middle.get_cellId());

                if (getCellCost(middleCell, true) < 2 && context.isCellWalkable(middleCell, false, cell))
                {
                    cells.add(middleCell);
                    i += 2;
                }
            }

            else if (i + 2 < len && node.cell.manhattanDistanceTo(nodes.get(i+2).cell) == 2)
            {
                final DofusCell middleCell = nodes.get(i+1).cell;
                final DofusCell nextCell = nodes.get(i+2).cell;
                final DofusCell middleCell2X = map.getCell(MapTools.getCellNumFromXYCoordinates(cell.getPoint().get_x(),middleCell.getPoint().get_y()));
                final DofusCell middleCell2Y = map.getCell(MapTools.getCellNumFromXYCoordinates(middleCell.getPoint().get_x(),cell.getPoint().get_y()));

                // cell aligned to nextcell but not to middle cell
                if (((cell.getPoint().get_x() + cell.getPoint().get_y() == nextCell.getPoint().get_x() + nextCell.getPoint().get_y() && cell.getPoint().get_x() - cell.getPoint().get_y() != middleCell.getPoint().get_x() - middleCell.getPoint().get_y()) ||
                        (cell.getPoint().get_x() - cell.getPoint().get_y() == nextCell.getPoint().get_x() - nextCell.getPoint().get_y() && cell.getPoint().get_x() - cell.getPoint().get_y() != middleCell.getPoint().get_x() - middleCell.getPoint().get_y() )) &&
                        !cell.isChangeZone(middleCell) &&
                        !middleCell.isChangeZone(nextCell))
                {
                    // then ignore middle cell
                    i++;
                }

                else if (cell.getPoint().get_x() == nextCell.getPoint().get_x() && cell.getPoint().get_x() != middleCell.getPoint().get_x() && getCellCost(middleCell2X, true) < 2 && context.isCellWalkable(middleCell2X, false, cell))
                {
                    cells.add(middleCell2X);
                    i++;
                }
                else if (cell.getPoint().get_y() == nextCell.getPoint().get_y() && cell.getPoint().get_y() != middleCell.getPoint().get_y() && getCellCost(middleCell2Y, true) < 2 && context.isCellWalkable(middleCell2Y, false, cell))
                {
                    cells.add(middleCell2Y);
                    i++;
                }
            }
        }

        return new Path(this.context, cells.stream().toArray(DofusCell[]::new));
    }

    private double getCellCost(DofusCell cell, boolean throughEntities)
    {
        byte speed = cell.getSpeed();

        if (throughEntities)
        {
            if (context.getCell(cell.getId()).getFighter() != null)
                return 20;

            if (speed >= 0)
                return 1 + 5 - speed;

            return 1 + 11 + Math.abs(speed);
        }

        double cost = 1d;
        DofusCell adjCell;

        if (context.getCell(cell.getId()).getFighter() != null)
            cost += 0.3;

        adjCell = map.getCell(MapTools.getCellNumFromXYCoordinates(cell.getPoint().get_x() + 1, cell.getPoint().get_y()));
        if (adjCell != null && context.getCell(adjCell.getId()).getFighter() != null)
            cost += 0.3;

        adjCell = map.getCell(MapTools.getCellNumFromXYCoordinates(cell.getPoint().get_x(), cell.getPoint().get_y() + 1));
        if (adjCell != null && context.getCell(adjCell.getId()).getFighter() != null)
            cost += 0.3;

        adjCell = map.getCell(MapTools.getCellNumFromXYCoordinates(cell.getPoint().get_x() - 1, cell.getPoint().get_y()));
        if (adjCell != null && context.getCell(adjCell.getId()).getFighter() != null)
            cost += 0.3;

        adjCell = map.getCell(MapTools.getCellNumFromXYCoordinates(cell.getPoint().get_x(), cell.getPoint().get_y() - 1));
        if (adjCell != null && context.getCell(adjCell.getId()).getFighter() != null)
            cost += 0.3;

        //TODO : Glyyphe
            /*if (context.IsCellMarked(cell))
                cost += 0.2;*/

        return cost;
    }

    public class ComparePfNodeMatrix implements Comparator<DofusCell>{

        private final PathNode[] m_matrix;

        public ComparePfNodeMatrix(PathNode[] matrix)
        {
            m_matrix = matrix;
        }

        @Override
        public int compare(DofusCell a, DofusCell b) {

            if (m_matrix[a.getId()].F() > m_matrix[b.getId()].F())
            {
                return 1;
            }

            if (m_matrix[a.getId()].F() < m_matrix[b.getId()].F())
            {
                return -1;
            }
            return 0;
        }
    }



}
