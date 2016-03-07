package koh.game.fights.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Created by Melancholia on 1/13/16.
 */
public class CellInfo {

    @Setter
    private Double heuristic;
    @Getter @Setter
    private int[] parent;
    @Getter @Setter
    private boolean opened, closed;
    @Getter @Setter
    private int movementCost;

    public CellInfo(Double pHeuristic, int[] pParent, boolean pOpened, boolean pClosed)
    {
        this.heuristic = pHeuristic;
        this.parent = pParent;
        this.opened = pOpened;
        this.closed = pClosed;
    }

    public double getHeuristic(){
        if(heuristic == null)
            return 0;
        return heuristic;
    }

}
