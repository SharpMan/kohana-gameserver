package koh.game.entities.maps.pathfinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import koh.game.Main;
import koh.game.dao.api.AccountDataDAO;
import koh.game.entities.environments.DofusCell;
import koh.game.entities.environments.DofusMap;
import koh.game.entities.environments.ObjectPosition;
import koh.protocol.client.enums.DirectionsEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Neo-Craft
 */
public class Path {

    private DofusCell[] m_cellsPath;
    private MapPoint[] m_path;
    private ObjectPosition[] m_compressedPath;
    private ObjectPosition m_endPathPosition;
    public DofusMap Map;

    private static final Logger logger = LogManager.getLogger(Path.class);

    public DofusCell getStartCell() {
        return this.m_cellsPath[0];
    }

    public DofusCell getEndCell() {
        return this.m_cellsPath[this.m_cellsPath.length - 1];
    }

    public ObjectPosition endPathPosition() {
        ObjectPosition arg_2A_0;
        if ((arg_2A_0 = this.m_endPathPosition) == null) {
            arg_2A_0 = (this.m_endPathPosition = new ObjectPosition(this.Map, this.getEndCell(), this.getEndCellDirection()));
        }
        return arg_2A_0;
    }

    public int mpCost() {
        return this.m_cellsPath.length - 1;
    }

    public Path(DofusMap map, DofusCell[] path) //List.ToArray<DofusCell>
    {
        this.Map = map;
        this.m_cellsPath = path;
        this.m_path = Arrays.stream(this.m_cellsPath).map(x -> MapPoint.fromCellId(x.getId())).collect(Collectors.toList()).toArray(new MapPoint[this.m_cellsPath.length]);
    }

    private Path(DofusMap map, ObjectPosition[] compressedPath) {
        this.Map = map;
        this.m_compressedPath = compressedPath;
        this.m_cellsPath = this.buildCompletePath();
        this.m_path = Arrays.stream(this.m_cellsPath).map(x -> MapPoint.fromCellId(x.getId())).collect(Collectors.toList()).toArray(new MapPoint[this.m_cellsPath.length]);
    }

    public boolean isEmpty() {
        return this.m_cellsPath.length == 0;
    }

    public ObjectPosition[] getCompressedPath() {
        ObjectPosition[] arg_19_0;
        if ((arg_19_0 = this.m_compressedPath) == null) {
            arg_19_0 = (this.m_compressedPath = this.buildCompressedPath());
        }
        return arg_19_0;
    }

    public DofusCell[] getPath() {
        return this.m_cellsPath;
    }

    public boolean Contains(short cellId) {
        return Arrays.stream(this.m_cellsPath).anyMatch(x -> x.getId() == cellId);
    }

    public Short[] getServerPathKeys() {
        return Arrays.stream(this.m_cellsPath).map(x -> x.getId()).collect(Collectors.toList()).toArray(new Short[this.m_cellsPath.length]);
    }

    public void cutPath(int index) {
        if (index <= this.m_cellsPath.length - 1) {
            //Take == get first element this.m_cellsPath = this.m_cellsPath.Take(index).ToArray<cell>();
            System.arraycopy(this.m_cellsPath, 0, this.m_cellsPath, 0, index);

            this.m_path = Arrays.stream(this.m_cellsPath).map(x -> MapPoint.fromCellId(x.getId())).collect(Collectors.toList()).toArray(new MapPoint[this.m_cellsPath.length]);

            this.m_endPathPosition = new ObjectPosition(this.Map, this.getEndCell(), this.getEndCellDirection());
        }
    }

    public int getEndCellDirection() {
        int result;
        if (this.m_cellsPath.length <= 1) {
            result = DirectionsEnum.RIGHT;
        } else {
            if (this.m_compressedPath != null) {
                result = this.m_compressedPath[this.m_compressedPath.length].direction;
                //result = this.m_compressedPath.Last<ObjectPosition>().direction;
            } else {
                result = this.m_path[this.m_path.length - 2].orientationTo(this.m_path[this.m_path.length - 1]);
            }
        }
        return result;
    }

    private ObjectPosition[] buildCompressedPath() {
        ObjectPosition[] result;
        if (this.m_cellsPath.length <= 0) {
            result = new ObjectPosition[0];
        } else {
            if (this.m_cellsPath.length <= 1) {
                result = new ObjectPosition[]{
                    new ObjectPosition(this.Map, this.m_cellsPath[0])
                };
            } else {
                List<ObjectPosition> list = new ArrayList<>();
                for (int i = 1; i < this.m_cellsPath.length; i++) {
                    list.add(new ObjectPosition(this.Map, this.m_cellsPath[i - 1], this.m_path[i - 1].orientationTo(this.m_path[i])));
                }
                list.add(new ObjectPosition(this.Map, this.m_cellsPath[this.m_cellsPath.length - 1], list.get(list.size() - 1).direction));
                if (list.size() > 0) {
                    for (int i = list.size() - 2; i > 0; i--) {
                        if (list.get(i).direction == list.get(i - 1).direction) {
                            list.remove(i);
                        }
                    }
                }
                result = list.toArray(new ObjectPosition[list.size()]);
            }
        }
        return result;
    }

    private DofusCell[] buildCompletePath() {
        List<DofusCell> list = new ArrayList<>();
        for (int i = 0; i < this.m_compressedPath.length - 1; i++) {
            list.add(this.m_compressedPath[i].cell);
            int num = 0;
            MapPoint mapPoint = this.m_compressedPath[i].point;
            while ((mapPoint = mapPoint.getNearestCellInDirection(this.m_compressedPath[i].direction)) != null && mapPoint.get_cellId() != this.m_compressedPath[i + 1].cell.getId()) {
                if ((long) num > 54L) {
                    logger.error("Path too long. Maybe an orientation problem ?");
                }
                list.add(this.Map.getCell((short) mapPoint.get_cellId()));
                num++;
            }
        }
        list.add(this.m_compressedPath[this.m_compressedPath.length - 1].cell);
        return (DofusCell[]) list.toArray(); //Lol it's work ?
    }

    public static Path buildFromCompressedPath(DofusMap map, short[] keys) {
        ObjectPosition[] compressedPath = new ObjectPosition[keys.length];
        int i = 0;
        for (short key : keys) {
            compressedPath[i] = new ObjectPosition(map, map.getCell((short) (key & 4095)), (int) key >> 12 & 7);
            i++;
        }
        return new Path(map, compressedPath);
    }

    public static Path getEmptyPath(DofusMap map, DofusCell startCell) {
        return new Path(map, new DofusCell[]{
            startCell
        });
    }

}
