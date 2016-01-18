package koh.game.entities.maps.pathfinding;

import koh.game.fights.Fight;
import koh.protocol.messages.game.context.ShowCellMessage;
import koh.utils.Couple;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Melancholia on 1/15/16.
 */
public class LosDetector {


    private static final boolean USE_DOFUS_2_LINE = true;

    public static Short[] getCell(Fight fight, Short[] range, MapPoint refPosition) {
        short i;
        List<Point> line;
        boolean los;
        String currentPoint = null;
        MapPoint p;
        int j;
        Object[] a = new Object[0];
        Couple[] orderedCell = new Couple[range.length];
        MapPoint mp;
        i = 0;

        while (i < range.length) {
            mp = MapPoint.fromCellId(range[i]);
            orderedCell[i] = new Couple<>(mp, refPosition.distanceToCell(mp));
            i++;
        }

        Arrays.sort(orderedCell, (e1, e2) -> Integer.compare((int) e1.second, (int) e2.second));

        final HashMap<String, Boolean> tested = new HashMap<>();

        Short[] result = new Short[1];
        i = 0;
        while (i < orderedCell.length) {
            p = (MapPoint) orderedCell[i].first;
            if (((((!((tested.get(((p.get_x() + "_") + p.get_y())) == null))) && (!(((refPosition.get_x() + refPosition.get_y()) == (p.get_x() + p.get_y())))))) && (!(((refPosition.get_x() - refPosition.get_y()) == (p.get_x() - p.get_y())))))) {
            } else {
                if (USE_DOFUS_2_LINE) {
                   // line = Dofus2Line.getLine(refPosition.get_cellId(), p.get_cellId());
                    line = Bresenham.findLine(refPosition.get_x(), refPosition.get_y(), p.get_x(), p.get_y());
                } else {
                    line = Dofus1Line.getLine(refPosition.get_x(), refPosition.get_y(), 0, p.get_x(), p.get_y(), 0);
                }

                if (line.size() == 0) {
                    result = ArrayUtils.add(result, p.get_cellId());
                } else {
                    los = true;
                    j = 0;
                    while (j < line.size()) {
                        currentPoint = (((int)Math.floor(line.get(j).x) + "_") + (int)Math.floor(line.get(j).y));
                        if (!(MapPoint.isInMap(line.get(j).x, line.get(j).y))) {
                        } else {
                            if ((((j > 0)) && (fight.hasEntity((int) Math.floor(line.get(j - 1).x), (int) Math.floor(line.get(j - 1).y))))) {
                                los = false;
                            } else {
                                if (((((line.get(j).x + line.get(j).y) == (refPosition.get_x() + refPosition.get_y()))) || (((line.get(j).x - line.get(j).y) == (refPosition.get_x() - refPosition.get_y()))))) {
                                    los = ((los) && (fight.pointLos((int) Math.floor(line.get(j).x), (int) Math.floor(line.get(j).y), true)));
                                } else {
                                    if (tested.get(currentPoint) == null) {
                                        los = ((los) && (fight.pointLos((int) Math.floor(line.get(j).x), (int) Math.floor(line.get(j).y), true)));
                                    } else {
                                        los = ((los) && (tested.get(currentPoint)));
                                    }
                                }
                            }
                        }
                        j++;
                    }
                    tested.put(currentPoint, los);
                }
            }
            ;
            i++;
        }
        i = 0;
        while (i < range.length) {
            mp = MapPoint.fromCellId(range[i]);
            if (tested.get((mp.get_x() + "_") + mp.get_y()) == Boolean.TRUE) {
                result = ArrayUtils.add(result, mp.get_cellId());
            }
            i++;
        }
        tested.clear();
        return (result);
    }
}



