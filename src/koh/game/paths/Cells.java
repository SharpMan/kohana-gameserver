package koh.game.paths;

import koh.game.entities.environments.*;
import koh.maths.Point;
import koh.maths.Vector;
import koh.protocol.client.enums.DirectionsEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Melancholia on 1/13/16.
 */
public class Cells {

    public static final short MAP_WIDTH = 14;
    public static final short MAP_HEIGHT = 20;

    public static final Map<Byte, Vector> VECTORS = new HashMap<Byte, Vector>() {{
        put(DirectionsEnum.RIGHT, Vector.create(1, -1));
        put(DirectionsEnum.DOWN_RIGHT, Vector.create(1, 0));
        put(DirectionsEnum.DOWN, Vector.create(1, 1));
        put(DirectionsEnum.DOWN_LEFT, Vector.create(0, 1));
        put(DirectionsEnum.LEFT, Vector.create(-1, 1));
        put(DirectionsEnum.UP_LEFT, Vector.create(-1, 0));
        put(DirectionsEnum.UP, Vector.create(-1, -1));
        put(DirectionsEnum.UP_RIGHT, Vector.create(0, -1));
    }};

    public static Point position(short cellId) {
        int _loc5 = (int) Math.floor(cellId / (MAP_WIDTH * 2 - 1));
        int _loc6 = cellId - _loc5 * (MAP_WIDTH * 2 - 1);
        int _loc7 = _loc6 % MAP_WIDTH;
        int x = (cellId - (MAP_WIDTH - 1) * (_loc5 - _loc7)) / MAP_WIDTH;
        int y = _loc5 - _loc7;

        return new Point(x, y);
    }


    public static Point position(Node node) {
        return position(node.getCell());
    }

    public static long estimateTime(Path path){
        long time = 0;
        int steps = path.size();

        for (int i = 0; i < steps - 1; ++i){
            Node current = path.get(i), next = path.get(i + 1);
            switch (next.getOrientation()){
                case DirectionsEnum.RIGHT:
                case DirectionsEnum.LEFT:
                    time += ( Math.abs(current.getCell() - next.getCell()) ) * (steps >= 4 ? 350 : 875);
                    break;

                case DirectionsEnum.DOWN:
                case DirectionsEnum.UP:
                    time += ( Math.abs(current.getCell() - next.getCell()) / ( MAP_WIDTH * 2 - 1 ) ) * (steps >= 4 ? 350 : 875);
                    break;

                case DirectionsEnum.UP_LEFT:
                case DirectionsEnum.DOWN_LEFT:
                    time += ( Math.abs(current.getCell() - next.getCell()) / ( MAP_WIDTH - 1 ) ) * (steps >= 4 ? 250 : 625);
                    break;

                case DirectionsEnum.UP_RIGHT:
                case DirectionsEnum.DOWN_RIGHT:
                    time += ( Math.abs(current.getCell() - next.getCell()) / ( MAP_WIDTH - 1) ) * (steps >= 4 ? 250 : 625);
                    break;
            }
        }

        return time;
    }

    public static short getCellIdByOrientation(short cellId, byte orientation) {
        return koh.game.entities.environments.Pathfinder.nextCell(cellId,orientation);
        /*
        switch (orientation) {
            case DirectionsEnum.RIGHT:
                return (short) (cellId + 1);
            case DirectionsEnum.DOWN_RIGHT:
                return (short) (cellId + MAP_WIDTH);
            case DirectionsEnum.DOWN:
                return (short) (cellId + (MAP_WIDTH * 2 - 1));
            case DirectionsEnum.DOWN_LEFT:
                return (short) (cellId + (MAP_WIDTH - 1));
            case DirectionsEnum.LEFT:
                return (short) (cellId - 1);
            case DirectionsEnum.UP_LEFT:
                return (short) (cellId - MAP_WIDTH);
            case DirectionsEnum.UP:
                return (short) (cellId - (MAP_WIDTH * 2 - 1));
            case DirectionsEnum.UP_RIGHT:
                return (short) (cellId - MAP_WIDTH + 1);

            default:
                throw new IllegalArgumentException("invalid orientation");
        }*/
    }

    public static short getCellIdByOrientation(Node node, byte orientation) {
        return getCellIdByOrientation(node.getCell(), orientation);
    }

    public static Byte getOrientationByPoints(Point a, Point b) {
        Vector vector = Vector.fromPoints(a, b);
        for (Map.Entry<Byte, Vector> entry : VECTORS.entrySet()) {
            if (entry.getValue().hasSameDirectionOf(vector)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static int distanceBetween(Point a, Point b) {
        return Math.abs(a.abscissa() - b.abscissa()) +
                Math.abs(a.ordinate() - b.ordinate());
    }

    public static int distanceBetween(Node a, Node b) {
        return distanceBetween(position(a), position(b));
    }

    public static int distanceBetween(Node a, short b) {
        return distanceBetween(position(a.getCell()), position(b));
    }


}
