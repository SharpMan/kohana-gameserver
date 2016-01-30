package koh.game.fights.utils;

import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.Pathfunction;
import koh.game.entities.maps.pathfinding.MapTools;
import koh.game.fights.Fight;
import koh.protocol.client.enums.DirectionsEnum;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Melancholia on 1/26/16.
 */
public class Path {

    @Getter
    private DofusCell[] cellsPath;

    private PathElement[] compressedPath;
    private final Fight map;

    public Path(Fight map, DofusCell[] path)
    {
        this.map = map;
        cellsPath = path;
    }

    public Path(Fight map, Stream<PathElement> compressedPath) {
        this.map = map;
        this.compressedPath = compressedPath.toArray(PathElement[]::new);
        cellsPath = buildCompletePath();
    }

    public DofusCell getStart()
    {
        if (cellsPath != null && cellsPath.length > 0) return cellsPath[0]; return null;
    }

    public DofusCell getEnd()
    {
        if (cellsPath != null && cellsPath.length > 0) return cellsPath[cellsPath.length - 1]; return null;
    }

    public void cutPath(int index) {
        if (index > this.cellsPath.length - 1) {
            return;
        }
        this.cellsPath = ArrayUtils.subarray(this.cellsPath,0,index);
    }

    public int MPCost()
    {
        return  getStart().manhattanDistanceTo(getEnd());
    }

    public boolean isEmpty()
    {
        return cellsPath == null || cellsPath.length <= 1; // if end == start the path is also empty
    }

    public byte getEndCellDirection()
    {
        if (cellsPath.length <= 1)
            return DirectionsEnum.RIGHT;

        if (compressedPath != null)
            return compressedPath[this.compressedPath.length -1].direction;

        return cellsPath[cellsPath.length - 2].orientationToAdjacent(cellsPath[cellsPath.length - 1]);
    }

   /* public double getHeuristic()
    {
        return EstimateHeuristic * pointA.ManhattanDistanceTo(pointB);
    }*/

    public int getMovementTime() {
        return (int) Pathfunction.getPathTime(this.MPCost());
    }

    public short[] getClientPathKeys(){
        final PathElement[] compressedPath = getCompressedPath();

        final short[] encodedPath = new short[getCompressedPath().length];
        for(int i = 0 ; i < encodedPath.length; i++){
            encodedPath[i] =(short)(compressedPath[i].cell.getId() | (compressedPath[i].direction << 12));
        }
        return encodedPath;
    }


    public PathElement[] getCompressedPath()
    {
        if(compressedPath == null)
            this.compressedPath = buildCompressedPath();
        return compressedPath;
    }

    private PathElement[] buildCompressedPath()
    {
        if (cellsPath.length <= 0)
            return new PathElement[0];

        // only one cell
        if (cellsPath.length <= 1)
            return new PathElement[] { new PathElement(cellsPath[0], DirectionsEnum.RIGHT) };

        // build the path
        List<PathElement> path = new ArrayList<>();
        for (int i = 1; i < cellsPath.length; i++)
        {
            path.add(new PathElement(cellsPath[i - 1], cellsPath[i - 1].orientationToAdjacent(cellsPath[i])));
        }

        path.add(new PathElement(cellsPath[cellsPath.length - 1], path.get(path.size() - 1).direction));

        // compress it
        if (path.size() > 0)
        {
            int i = path.size() - 2; // we don't touch to the last vector
            while (i > 0)
            {
                if (path.get(i).direction == path.get(i -1).direction)
                    path.remove(i);
                i--;
            }
        }

        return path.stream().toArray(PathElement[]::new);
    }



    public DofusCell[] buildCompletePath()
    {
        List<DofusCell> completePath = new ArrayList<>();

        for (int i = 0; i < compressedPath.length - 1; i++)
        {
            completePath.add(compressedPath[i].cell);

            int l = 0;
            DofusCell nextPoint = compressedPath[i].cell;
            while ((nextPoint = nextPoint.getNearestCellInDirection(compressedPath[i].direction)) != null &&
                    nextPoint.getId() != compressedPath[i + 1].cell.getId())
            {
                if (l > MapTools.HEIGHT * 2 + MapTools.WIDTH)
                    throw new Error("Path too long. Maybe an orientation problem ?");

                completePath.add(map.getMap().getCell(nextPoint.getId()));

                l++;
            }
        }

        completePath.add(compressedPath[compressedPath.length - 1].cell);

        return completePath.stream().toArray(DofusCell[]::new);
    }

    /*public static Path buildFromServerCompressedPath(Fight map, short[] keys)
    {
        DofusCell[] cells = Arrays.stream(keys).map(entry -> map.getMap().getCell(entry)).toArray(DofusCell[]::new);
        List<PathElement> compressedPath = new ArrayList<>();

        for (int i = 0; i < cells.length - 1; i++)
        {
            compressedPath.add(new PathElement(cells[i], cells[i].orientationTo(cells[i + 1])));
        }

        compressedPath.add(new PathElement(cells[cells.length - 1], DirectionsEnum.RIGHT));

        return new Path(map, compressedPath.stream());
    }*/

    public static Path getEmptyPath(Fight map, DofusCell startCell)
    {
        return new Path(map, new DofusCell[] { startCell });
    }

    @Override
    public String toString()
    {
        return StringUtils.join(cellsPath,'-');
    }




}
