package koh.game.entities.maps.pathfinding;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Melancholia on 1/16/16.
 */
public class Dofus2Line {

    public static ArrayList<Point> getLine(short startCellId, short endCellId){
        return MapTools.getLOSCellsVector(startCellId, endCellId);
    }


}
