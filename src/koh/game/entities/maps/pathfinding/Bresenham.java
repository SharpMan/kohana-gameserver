package koh.game.entities.maps.pathfinding;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Melancholia on 1/17/16.
 */
public class Bresenham {

    private static final Map<Integer,Map<Integer, Point>> GRID = new HashMap<>(600);


    private static final Point getPoint(int x, int y){
        if(!GRID.containsKey(x)){
            GRID.put(x, new HashMap<Integer, Point>(){{
                this.put(y, new Point(x,y));
            }});
        }else if (!GRID.get(x).containsKey(y)){
            GRID.get(x).put(y, new Point(x,y));
        }
        return GRID.get(x).get(y);
    }


    public static List<Point> findLine(int x0, int y0, int x1, int y1)
    {
        List<Point> line = new ArrayList<Point>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true)
        {
            line.add(getPoint(x0,y0));

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy)
            {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx)
            {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        line.remove(0);
        return line;
    }

}
